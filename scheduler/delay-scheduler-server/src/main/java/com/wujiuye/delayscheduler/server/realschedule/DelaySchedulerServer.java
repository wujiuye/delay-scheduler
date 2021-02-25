package com.wujiuye.delayscheduler.server.realschedule;

import com.github.wujiuye.transport.netty.commom.JsonUtils;
import com.wujiuye.delayscheduler.core.ActionLog;
import com.wujiuye.delayscheduler.core.ActionStat;
import com.wujiuye.delayscheduler.server.common.LoggerUtils;
import com.wujiuye.delayscheduler.server.common.RaftUtils;
import com.wujiuye.delayscheduler.server.hashedwheeltimer.HashedWheelCallback;
import com.wujiuye.delayscheduler.server.hashedwheeltimer.HashedWheelTimer;
import com.wujiuye.delayscheduler.server.hashedwheeltimer.TimerTask;
import com.wujiuye.delayscheduler.server.stroage.KeyValueStorage;
import com.wujiuye.delayscheduler.server.stroage.Leveldb;
import com.wujiuye.delayscheduler.server.stroage.LeveldbKeyValueStorage;
import org.iq80.leveldb.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 调度器
 *
 * @author wujiuye 2021/01/20
 */
public class DelaySchedulerServer implements Runnable, HashedWheelCallback, Closeable {

    private HashedWheelTimer wheelTimer;
    private Map<String, Queue<Long>> tasksMap;
    private Thread thread;
    private AtomicBoolean status = new AtomicBoolean(Boolean.TRUE);
    private KeyValueStorage storage;
    private RecoveryActionThread recoveryActionThread;
    private ClearHistoricalActionThread clearHistoricalActionThread;

    public DelaySchedulerServer() {
        this.wheelTimer = new HashedWheelTimer(this);
        this.tasksMap = new HashMap<>();
        this.storage = new LeveldbKeyValueStorage();
        this.thread = new Thread(this::run, "delay-scheduler");
        this.thread.setDaemon(true);
        this.thread.start();
        this.recoveryActionThread = new RecoveryActionThread();
        this.recoveryActionThread.start();
        this.clearHistoricalActionThread = new ClearHistoricalActionThread();
        this.clearHistoricalActionThread.start();
    }

    public boolean putAction(String application, long actionId, ActionLog actionLog) {
        long submitDate = actionLog.getRetrySubmitDate() > 0 ?
                // 取重试的提交时间
                actionLog.getRetrySubmitDate()
                // 取首次提交时间
                : actionLog.getSubmitDate();
        long nextRunTime = submitDate + actionLog.getFreePeriods().get(actionLog.getNextPeriodIndex());
        // 大于60秒的先等等
        if (nextRunTime - System.currentTimeMillis() / 1000 > 60) {
            return false;
        }
        wheelTimer.putTask(application + "::" + actionId, submitDate,
                actionLog.getFreePeriods().get(actionLog.getNextPeriodIndex()));
        return true;
    }

    @Override
    public void close() {
        LoggerUtils.getLogger().debug("delay schedule server close...");
        this.wheelTimer.close();
        this.suspend();
        this.thread.interrupt();
        this.recoveryActionThread.close();
        this.clearHistoricalActionThread.close();
    }

    @Override
    public void runWork(long rounds, List<TimerTask> tasks) {
        try {
            RaftUtils.checkMaster();
        } catch (Throwable throwable) {
            synchronized (this) {
                tasksMap.forEach((key, value) -> {
                    LoggerUtils.getLogger().error("the current node is not leader. clear works {} by application {}", value.size(), key);
                    value.clear();
                });
                tasksMap.clear();
            }
            return;
        }
        tasks.forEach(timerTask -> {
            String[] key = timerTask.getTaskId().split("::");
            long id = Long.parseLong(key[1]);
            String application = key[0];
            synchronized (this) {
                if (!tasksMap.containsKey(application)) {
                    tasksMap.put(application, new LinkedBlockingQueue<>());
                }
                tasksMap.get(application).add(id);
            }
        });
    }

    public List<Long> poll(String application, int maxRecords) {
        Queue<Long> queue;
        synchronized (this) {
            queue = tasksMap.get(application);
        }
        if (queue == null) {
            return Collections.emptyList();
        }
        List<Long> result = new ArrayList<>(Math.min(maxRecords, queue.size()));
        for (int i = 0; i < maxRecords && queue.size() > 0; i++) {
            Long id = queue.remove();
            if (id == null) {
                break;
            }
            result.add(id);
        }
        return result;
    }

    public void suspend() {
        status.set(Boolean.FALSE);
    }

    public void recovery() {
        status.set(Boolean.TRUE);
    }

    @Override
    public void run() {
        out:
        while (!Thread.interrupted()) {
            try {
                RaftUtils.checkMaster();
                recovery();
            } catch (Exception ignored) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException ignored1) {
                }
                continue;
            }
            while (status.get()) {
                DB db = Leveldb.getDatabase();
                if (db == null) {
                    LoggerUtils.getLogger().error("db already close.");
                    break out;
                }
                try (Snapshot snapshot = db.getSnapshot();
                     DBIterator dbIterator = db.iterator(new ReadOptions().snapshot(snapshot))) {
                    // 搜索pending状态的action
                    String prefix = ActionStat.PENDING.getStat() + "::";
                    dbIterator.seek(prefix.getBytes());
                    while (dbIterator.hasNext()) {
                        Map.Entry<byte[], byte[]> entry = dbIterator.next();
                        String[] keyInfo = new String(entry.getKey()).split("::");
                        String application = keyInfo[1];
                        long id = Long.parseLong(keyInfo[2]);
                        String value = new String(entry.getValue());
                        ActionLog actionLog = JsonUtils.fromJson(value, ActionLog.class);
                        if (!actionLog.getStatus().equals(ActionStat.PENDING.getStat())) {
                            break;
                        }
                        // 判断是否可以放入时间轮
                        if (putAction(application, id, actionLog)) {
                            System.out.println("put action: " + value);
                            // 修改状态为调度中
                            actionLog.setStatus(ActionStat.DISPATCHING.getStat());
                            this.storage.save(application, keyInfo[2], actionLog);
                        }
                    }
                } catch (DBException | IOException ex) {
                    LoggerUtils.getLogger().error("db exception:" + ex.getMessage());
                    break out;
                }
                try {
                    RaftUtils.checkMaster();
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (Throwable throwable) {
                    suspend();
                }
            }
        }
    }

}
