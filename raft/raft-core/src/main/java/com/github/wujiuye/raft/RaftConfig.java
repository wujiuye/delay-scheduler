package com.github.wujiuye.raft;

import java.util.Random;

/**
 * Raft配置
 *
 * @author wujiuye 2020/12/15
 */
public class RaftConfig {

    /**
     * 当前节点的ID
     */
    private int id = Integer.getInteger("raft.node.id", new Random().nextInt(Integer.MAX_VALUE));

    /**
     * 选举定时器使用，每个节点的选举定时都不相等，单位毫秒
     * 如果不为每个节点配置，则从范围取一个随机值
     */
    private long electionMs = Long.getLong("raft.electionMs", new Random().nextInt(50) + 150);

    /**
     * 广播心跳的周期，单位毫秒
     * 必须要小于选举定时器的超时时间（electionMs），即小于所有Raft节点的最小electionMs，也就是取最小值（Min(150,200)）
     */
    private long heartbeatMs = Long.getLong("raft.heartbeatMs", 150 / 2);

    RaftConfig() {

    }

    public void setId(int id) {
        this.id = id;
    }

    public void setHeartbeatMs(long heartbeatMs) {
        this.heartbeatMs = heartbeatMs;
    }

    public void setElectionMs(long electionMs) {
        this.electionMs = electionMs;
    }

    public int getId() {
        return id;
    }

    public long getElectionMs() {
        return electionMs;
    }

    public long getHeartbeatMs() {
        return heartbeatMs;
    }

}
