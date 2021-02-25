package com.wujiuye.delayscheduler.client.work;

import com.github.wujiuye.transport.netty.commom.JsonUtils;
import com.wujiuye.delayscheduler.core.Action;
import com.wujiuye.delayscheduler.core.ActionAdapter;
import com.wujiuye.delayscheduler.core.ActionBindManager;
import com.wujiuye.delayscheduler.core.ActionLog;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author wujiuye 2021/01/20
 */
public class DefaultWorker implements Worker {

    private ExecutorService executorService;
    private final String application;

    public DefaultWorker(String application, int threads) {
        this.application = application;
        this.executorService = new ThreadPoolExecutor(threads, threads, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadPoolExecutor.DiscardPolicy());
    }

    @Override
    public void close() throws IOException {
        this.executorService.shutdownNow();
    }

    @Override
    public void runAction(ActionLog actionLog, ActionExecResultCallback callback) {
        Action<?> action = ActionBindManager.getAction(application, actionLog.getTaskName());
        if (action == null) {
            return;
        }
        Action<Object> newAction = new ActionAdapter<Object>((Action<Object>) action) {
            @Override
            public Object getParam() {
                return JsonUtils.fromJson(actionLog.getParam(), action.getParamType());
            }
        };
        executorService.execute(() -> {
            boolean result = false;
            Throwable throwable = null;
            try {
                int curPeriod = actionLog.getNextPeriodIndex();
                result = newAction.execute(curPeriod, newAction.getParam());
            } catch (Throwable ex) {
                throwable = ex;
            }
            callback.onExecFinish(actionLog.getId(), result, throwable);
        });
    }

}
