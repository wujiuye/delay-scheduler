package com.github.wujiuye.raft;

/**
 * 负责执行已经commit的日记条目
 *
 * @author wujiuye 2020/12/16
 */
public interface StateMachine {

    /**
     * 状态机执行日记（apply）
     *
     * @param execLog 日记条目
     */
    void apply(CommandLog execLog);

}
