package com.wujiuye.delayscheduler.client.work;

import com.wujiuye.delayscheduler.core.ActionLog;

import java.io.Closeable;

/**
 * @author wujiuye 2021/01/20
 */
public interface Worker extends Closeable {

    /**
     * 执行任务
     *
     * @param actionLog 任务
     * @param callback  异步回调
     */
    void runAction(ActionLog actionLog, ActionExecResultCallback callback);

}
