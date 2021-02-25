package com.wujiuye.delayscheduler.core.service;

/**
 * 适配器模式
 *
 * @author wujiuye 2020/08/21
 */
public interface ScheduleService extends FreeDelayScheduleService, TimingScheduleService {

    /**
     * 取消Action
     *
     * @param actionId 提交成功时返回的ID
     * @return 是否取消成功（任务已经调度完成、任务正在执行等情况下无法取消）
     */
    boolean cancelAction(long actionId);

}
