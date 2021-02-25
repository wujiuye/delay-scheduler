package com.wujiuye.delayscheduler.core;

/**
 * @author wujiuye 2020/08/24
 */
public interface Action<T> {

    /**
     * 任务名称，必须全局唯一
     *
     * @return
     */
    String getTaskName();

    /**
     * 获取参数类型
     *
     * @return
     */
    Class<T> getParamType();

    /**
     * 获取任务参数，execute允许参数为null时可返回null
     *
     * @return
     */
    T getParam();

    /**
     * 执行任务
     *
     * @param curPeriod 当前执行的周期（索引）
     * @param param     getParam方法返回的参数
     * @return
     */
    boolean execute(int curPeriod, T param);

}
