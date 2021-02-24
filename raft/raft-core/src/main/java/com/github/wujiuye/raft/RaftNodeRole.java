package com.github.wujiuye.raft;

/**
 * Raft节点角色
 *
 * @author wujiuye 2020/12/15
 */
public enum RaftNodeRole {

    /**
     * 领导人
     */
    Leader,
    /**
     * 候选人
     */
    Candidate,
    /**
     * 群众
     */
    Follower

}
