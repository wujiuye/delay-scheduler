package com.github.wujiuye.raft.rpc.vote;

/**
 * 拉票请求
 * 发起/调用方：候选人
 * 接收方：集群内的所有其他节点（除自己外）
 *
 * @author wujiuye 2020/12/15
 */
public class RequestVote {

    /**
     * 候选人的任期号
     */
    private long term;
    /**
     * 发起请求的候选人I（拉票方）
     */
    private int candidateId;
    /**
     * 候选人（拉票方）最新日记条目的索引值
     */
    private long lastLogIndex;
    /**
     * 候选人（拉票方）最新日记条目对应的任期号
     */
    private long lastLogTerm;

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public long getLastLogIndex() {
        return lastLogIndex;
    }

    public void setLastLogIndex(long lastLogIndex) {
        this.lastLogIndex = lastLogIndex;
    }

    public long getLastLogTerm() {
        return lastLogTerm;
    }

    public void setLastLogTerm(long lastLogTerm) {
        this.lastLogTerm = lastLogTerm;
    }

    @Override
    public String toString() {
        return "RequestVote{" +
                "term=" + term +
                ", candidateId=" + candidateId +
                ", lastLogIndex=" + lastLogIndex +
                ", lastLogTerm=" + lastLogTerm +
                '}';
    }

}
