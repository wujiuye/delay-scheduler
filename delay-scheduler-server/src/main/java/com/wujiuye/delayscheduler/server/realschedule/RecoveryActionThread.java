package com.wujiuye.delayscheduler.server.realschedule;

import com.github.wujiuye.transport.netty.commom.JsonUtils;
import com.wujiuye.delayscheduler.core.ActionLog;
import com.wujiuye.delayscheduler.core.ActionStat;
import com.wujiuye.delayscheduler.server.common.LoggerUtils;
import com.wujiuye.delayscheduler.server.common.RaftUtils;
import com.wujiuye.delayscheduler.server.stroage.KeyValueStorage;
import com.wujiuye.delayscheduler.server.stroage.Leveldb;
import com.wujiuye.delayscheduler.server.stroage.LeveldbKeyValueStorage;
import org.iq80.leveldb.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 定时恢复锁定记录
 *
 * @author wujiuye 2021/02/05
 */
public class RecoveryActionThread extends Thread implements Closeable {

    private KeyValueStorage storage;

    public RecoveryActionThread() {
        super("delay-recovery-action");
        setDaemon(true);
        this.storage = new LeveldbKeyValueStorage();
    }

    @Override
    public void run() {
        recoveryAction();
    }

    public void recoveryAction() {
        while (!Thread.interrupted()) {
            // 只有主节点才执行此任务
            try {
                RaftUtils.checkMaster();
            } catch (Throwable ignored) {
                try {
                    TimeUnit.MILLISECONDS.sleep(60 * 1000L);
                } catch (InterruptedException ignored1) {
                }
                continue;
            }
            // db已经关闭则退出
            DB db = Leveldb.getDatabase();
            if (db == null) {
                LoggerUtils.getLogger().error("db already close.");
                return;
            }
            // 扫描DISPATCHING状态的action
            try (Snapshot snapshot = db.getSnapshot();
                 DBIterator dbIterator = db.iterator(new ReadOptions().snapshot(snapshot))) {
                String prefix = ActionStat.DISPATCHING.getStat() + "::";
                dbIterator.seek(prefix.getBytes());
                while (dbIterator.hasNext()) {
                    Map.Entry<byte[], byte[]> entry = dbIterator.next();
                    String[] keyInfo = new String(entry.getKey()).split("::");
                    String application = keyInfo[1];
                    String value = new String(entry.getValue());
                    ActionLog actionLog = JsonUtils.fromJson(value, ActionLog.class);
                    if (!actionLog.getStatus().equals(ActionStat.DISPATCHING.getStat())) {
                        break;
                    }
                    // 当前时间已经超过下次执行时间的15分钟，视该action为调度失败；
                    // 如果当前节点还是主节点，则恢复action为pending状态；
                    long submitTimeSecond = actionLog.getRetrySubmitDate() > 0
                            ? actionLog.getRetrySubmitDate() : actionLog.getSubmitDate();
                    long nextRuntime = submitTimeSecond + actionLog.getFreePeriods().get(actionLog.getNextPeriodIndex());
                    if (nextRuntime - System.currentTimeMillis() / 1000 > 15 * 60) {
                        try {
                            RaftUtils.checkMaster();
                            actionLog.setStatus(ActionStat.DISPATCHING.getStat());
                            this.storage.save(application, keyInfo[2], actionLog);
                        } catch (Throwable throwable) {
                            break;
                        }
                    }
                }
            } catch (DBException | IOException ex) {
                LoggerUtils.getLogger().error("db exception:" + ex.getMessage());
                return;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(60 * 1000L);
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void close() {
        this.interrupt();
    }

}
