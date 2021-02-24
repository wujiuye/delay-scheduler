package com.github.wujiuye.raft.common;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 每期自己拥有的选票
 *
 * @author wujiuye 2020/12/15
 */
public class TermVoter {

    private long term;
    /**
     * 获取选票的节点的ID
     */
    private int voteNodeId;
    /**
     * 投票标志，是否已经投票了
     */
    private AtomicBoolean alreadyVoted = new AtomicBoolean(Boolean.FALSE);

    TermVoter(long term) {
        this.term = term;
    }

    public long getTerm() {
        return term;
    }

    /**
     * 锁定投票状态
     *
     * @return true:获取选票成功，false:获取选票失败
     */
    public boolean lockVote(int nodeId) {
        if (alreadyVoted.compareAndSet(Boolean.FALSE, Boolean.TRUE)) {
            voteNodeId = nodeId;
            return true;
        }
        return false;
    }

    /**
     * 获取取得选票的节点的ID
     *
     * @return
     */
    public int getVoteNodeId() {
        return voteNodeId;
    }

}
