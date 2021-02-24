package com.github.wujiuye.raft.rpc.replication;

import com.github.wujiuye.raft.CommandLog;

import java.util.Arrays;

/**
 * 领导人向其它Raft节点发起rpc请求，要求其它节点复制这个日记条目
 *
 * @author wujiuye 2020/12/15
 */
public class AppendEntries implements Cloneable {

    /**
     * 领导人创建该条目时的任期号
     */
    private long term;
    /**
     * 领导人的ID，为了其它Raft节点能够重定向客户端请求
     */
    private int leaderId;
    /**
     * 领导人已提交的日记中最新一条日记的索引
     */
    private long prevLogIndex;
    /**
     * 领导人已提交的日记中最新一条日记的任期号
     */
    private long prevLogTerm;
    /**
     * 领导人为每个Follower都维护一个leaderCommit，
     * 表示领导人认为Follower已经提交的日记条目索引值
     */
    private long leaderCommit;
    /**
     * 将要追加到Follower上的日记条目。
     * 如果是心跳包，则entries为空
     */
    private CommandLog[] entries;

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public int getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
    }

    public long getPrevLogIndex() {
        return prevLogIndex;
    }

    public void setPrevLogIndex(long prevLogIndex) {
        this.prevLogIndex = prevLogIndex;
    }

    public long getPrevLogTerm() {
        return prevLogTerm;
    }

    public void setPrevLogTerm(long prevLogTerm) {
        this.prevLogTerm = prevLogTerm;
    }

    public long getLeaderCommit() {
        return leaderCommit;
    }

    public void setLeaderCommit(long leaderCommit) {
        this.leaderCommit = leaderCommit;
    }

    public CommandLog[] getEntries() {
        return entries;
    }

    public void setEntries(CommandLog[] entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        return "AppendEntries{" +
                "term=" + term +
                ", leaderId=" + leaderId +
                ", prevLogIndex=" + prevLogIndex +
                ", prevLogTerm=" + prevLogTerm +
                ", leaderCommit=" + leaderCommit +
                ", entries=" + Arrays.toString(entries) +
                '}';
    }

    @Override
    protected AppendEntries clone() {
        AppendEntries appendEntries = new AppendEntries();
        appendEntries.setTerm(this.getTerm());
        appendEntries.setLeaderId(this.getLeaderId());
        appendEntries.setLeaderCommit(this.getLeaderCommit());
        appendEntries.setPrevLogTerm(this.getPrevLogTerm());
        appendEntries.setPrevLogIndex(this.getPrevLogIndex());
        appendEntries.setEntries(this.getEntries());
        return appendEntries;
    }

}
