package com.wujiuye.delayscheduler.core.service;

import com.wujiuye.delayscheduler.core.Action;

import java.time.LocalDateTime;

/**
 * 定时调度
 *
 * @author wujiuye 2020/12/14
 */
public interface TimingScheduleService {

    /**
     * 提交定时调度操作
     *
     * @param action   当action返回true时，结束回调
     * @param datetime 执行的时间
     * @return 任务唯一id
     */
    <T> long submitAction(Action<T> action, LocalDateTime datetime);

}
