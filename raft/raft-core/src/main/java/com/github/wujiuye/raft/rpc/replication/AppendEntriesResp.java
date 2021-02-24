package com.github.wujiuye.raft.rpc.replication;

/**
 * AppendEntries RPC响应
 *
 * @author wujiuye 2020/12/15
 */
public class AppendEntriesResp {

    /**
     * 当前任期号
     * 取值：
     * Max(AppendEntries请求data携带的term，Follower本地维护的term)
     * 用于领导人更新自己的任期号，一旦领导人发现当前任期号
     * 比自己的要大，就表明自己是一个过时的领导人，需要停止
     * 发送AppendEntries RPC请求，主动切换为Follower
     */
    private long term;

    /**
     * 接收者（Follower）是否能够匹配prevLogIndex和prevLogTerm
     */
    private boolean success;

    // 反序列化
    public AppendEntriesResp() {

    }

    public AppendEntriesResp(long term, boolean success) {
        this.term = term;
        this.success = success;
    }

    public long getTerm() {
        return term;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return "AppendEntriesResp{" +
                "term=" + term +
                ", success=" + success +
                '}';
    }

}
