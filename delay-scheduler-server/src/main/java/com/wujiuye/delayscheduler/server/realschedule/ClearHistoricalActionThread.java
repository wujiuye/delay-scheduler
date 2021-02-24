package com.wujiuye.delayscheduler.server.realschedule;

import com.github.wujiuye.transport.netty.commom.JsonUtils;
import com.wujiuye.delayscheduler.core.ActionLog;
import com.wujiuye.delayscheduler.core.ActionStat;
import com.wujiuye.delayscheduler.server.common.LoggerUtils;
import com.wujiuye.delayscheduler.server.stroage.Leveldb;
import org.iq80.leveldb.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 清除历史数据
 *
 * @author wujiuye 2021/02/05
 */
public class ClearHistoricalActionThread extends Thread implements Closeable {

    public ClearHistoricalActionThread() {
        super("clear-historical-action");
        setDaemon(true);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            DB db = Leveldb.getDatabase();
            if (db == null) {
                LoggerUtils.getLogger().error("db already close.");
                return;
            }
            long curTimeSencond = System.currentTimeMillis() / 1000;
            try (Snapshot snapshot = db.getSnapshot();
                 DBIterator dbIterator = db.iterator(new ReadOptions().snapshot(snapshot))) {
                while (dbIterator.hasNext()) {
                    Map.Entry<byte[], byte[]> entry = dbIterator.next();
                    String value = new String(entry.getValue());
                    ActionLog actionLog = JsonUtils.fromJson(value, ActionLog.class);
                    long submitTime = actionLog.getRetrySubmitDate() > 0
                            ? actionLog.getRetrySubmitDate() : actionLog.getSubmitDate();
                    if (curTimeSencond < submitTime + (24 * 60 * 60) * 31) {
                        continue;
                    }
                    ActionStat stat = ActionStat.valueBy(actionLog.getStatus());
                    if (stat == null) {
                        continue;
                    }
                    switch (stat) {
                        case FAIL:
                        case CANCEL:
                        case SUCCESS:
                            db.delete(entry.getKey());
                            break;
                        default:
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                }
            } catch (DBException | IOException ex) {
                LoggerUtils.getLogger().error("db exception:" + ex.getMessage());
                return;
            }
            try {
                TimeUnit.DAYS.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Override
    public void close() {
        this.interrupt();
    }

}
