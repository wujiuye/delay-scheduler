package com.wujiuye.delayscheduler.client.work;

/**
 * @author wujiuye 2021/01/20
 */
public interface ActionExecResultCallback {

    /**
     * 执行回调
     *
     * @param actionId
     * @param success
     * @param throwable
     */
    void onExecFinish(long actionId, boolean success, Throwable throwable);

}
