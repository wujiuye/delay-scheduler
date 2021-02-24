package com.github.wujiuye.raft.rpc.vote;

import com.github.wujiuye.raft.RaftNodeRole;

/**
 * 选举节点监听器
 *
 * @author wujiuye 2020/12/16
 */
public interface VoteListener {

    /**
     * 开始选举
     */
    void onVoteStart();

    /**
     * 选举结束时调用
     *
     * @param curRaftNodeRole 当前节点的角色
     */
    void onVoteEnd(RaftNodeRole curRaftNodeRole);

}
