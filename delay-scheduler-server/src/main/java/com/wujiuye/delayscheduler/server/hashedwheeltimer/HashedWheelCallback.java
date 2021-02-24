package com.wujiuye.delayscheduler.server.hashedwheeltimer;

import java.util.List;

/**
 * @author wujiuye 2021/01/15
 */
@FunctionalInterface
public interface HashedWheelCallback {

    /**
     * 执行任务
     *
     * @param tasks 到时间需要执行的所有任务
     */
    void runWork(long rounds, List<TimerTask> tasks);

}
