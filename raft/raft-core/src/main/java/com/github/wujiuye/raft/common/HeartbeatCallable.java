package com.github.wujiuye.raft.common;

/**
 * @author wujiuye 2021/01/07
 */
public interface HeartbeatCallable {

    /**
     * 接收到心跳包时回调
     */
    void onHeartbeat();

}
