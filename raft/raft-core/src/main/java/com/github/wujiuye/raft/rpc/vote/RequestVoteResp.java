package com.github.wujiuye.raft.rpc.vote;

/**
 * 拉票响应
 *
 * @author wujiuye 2020/12/15
 * @see RequestVote
 */
public class RequestVoteResp {

    /**
     * 当前任期号，用于候选人更新自己本地的term值
     */
    private long term;
    /**
     * 如果候选人得到Follower的这张选票，则为true，否则为false
     */
    private boolean voteGranted;

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }

    public void setVoteGranted(boolean voteGranted) {
        this.voteGranted = voteGranted;
    }

    @Override
    public String toString() {
        return "RequestVoteResp{" +
                "term=" + term +
                ", voteGranted=" + voteGranted +
                '}';
    }

}
