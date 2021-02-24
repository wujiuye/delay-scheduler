package com.wujiuye.delayscheduler.client.work;

import com.wujiuye.delayscheduler.core.ActionLog;
import com.wujiuye.delayscheduler.core.rpc.ActionRpcInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * @author wujiuye 2021/01/20
 */
public final class WorkManager implements Runnable, Closeable, ActionExecResultCallback {

    private final static Logger logger = LoggerFactory.getLogger(WorkManager.class);

    private String application;
    private ActionRpcInterface actionRpcInterface;
    private Thread thread;
    private volatile boolean status = true;
    private DefaultWorker defaultWorker;

    public WorkManager(String application, ActionRpcInterface actionRpcInterface) {
        this.application = application;
        this.actionRpcInterface = actionRpcInterface;
        this.defaultWorker = new DefaultWorker(application, Runtime.getRuntime().availableProcessors() * 2);
        this.thread = new Thread(this::run, "work-manager");
        this.thread.setDaemon(true);
        this.thread.start();
    }

    @Override
    public void close() throws IOException {
        this.status = false;
        this.defaultWorker.close();
    }

    @Override
    public void run() {
        final int maxRecord = 1000;
        long curSecond = System.currentTimeMillis() / 1000;
        while (status) {
            try {
                List<ActionLog> actionLogList = actionRpcInterface.pull(application, maxRecord);
                if (!actionLogList.isEmpty()) {
                    actionLogList.parallelStream().forEach(actionLog -> defaultWorker.runAction(actionLog, this));
                }
                long curMs = System.currentTimeMillis();
                if (actionLogList.size() < maxRecord && (curSecond >= curMs / 1000)) {
                    long waitMs = curMs % 1000;
                    if (waitMs > 0) {
                        Thread.sleep(waitMs);
                    }
                }
            } catch (Exception exception) {
                if ("连接异常".equals(exception.getMessage())) {
                    continue;
                }
                logger.error(exception.getMessage());
            }
        }
    }

    @Override
    public void onExecFinish(long actionId, boolean success, Throwable throwable) {
        actionRpcInterface.commit(application, actionId, success);
    }

}
