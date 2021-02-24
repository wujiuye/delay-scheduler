package com.wujiuye.delayscheduler.core.service;

import com.wujiuye.delayscheduler.core.Action;

import java.util.concurrent.TimeUnit;

/**
 * 自定义周期延迟调度
 *
 * @author wujiuye 2020/08/21
 */
public interface FreeDelayScheduleService {

    /**
     * 提交调度操作
     *
     * @param action   当action返回true时，结束回调
     * @param timeUnit periods的单位
     * @param periods  延迟调度周期，例如：2,4,6,8,10
     *                 如果最后一次重试都失败，则不会再重试，否则只要重试调用action返回true则自动停止
     * @return 任务唯一id
     */
    <T> long submitAction(Action<T> action, TimeUnit timeUnit, long... periods);

}
