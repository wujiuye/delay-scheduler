package com.wujiuye.delayscheduler.core.rpc;

import com.wujiuye.delayscheduler.core.ActionLog;

import java.util.List;

/**
 * @author wujiuye 2021/01/12
 */
public interface ActionRpcInterface {

    /**
     * 提交
     *
     * @param application 名称空间
     * @param actionLog
     * @return
     */
    Long submit(String application, ActionLog actionLog);

    /**
     * 取消
     *
     * @param application 名称空间
     * @param actionId
     * @return
     */
    Boolean cancel(String application, Long actionId);

    /**
     * 拉取任务
     *
     * @param application 名称空间
     * @param maxRecord
     * @return
     */
    List<ActionLog> pull(String application, Integer maxRecord);

    /**
     * 提交任务执行结果
     *
     * @param application 名称空间
     * @param actionId
     * @param success
     */
    void commit(String application, Long actionId, Boolean success);

}
