package com.github.wujiuye.raft.rpc.replication;

/**
 * 由领导人向其它节点发起请求
 * 用于向其它节点同步日记（追加日记）
 * 调用方：Leader
 * 接收方：Follower
 *
 * @author wujiuye 2020/12/15
 */
public interface AppendEntriesRpc {

    /**
     * 请求追加日记条目
     * （除了用于复制日记，还用于广播Leader的心跳包）
     * 请求接收者（Follower）需实现：
     * 1）、如果自身的term大于AppendEntries#term，
     * 另领导人的任期号小于自身，则返回自身的term，且success为false；
     * 2）、否则，如果Follower自身在prevLogIndex
     * 日记的任期号与prevLogTerm不匹配，
     * 返回自身的term，且success为false；
     * 3）、否则，Follower进行日记一致性检查；
     * 4）、添加任何在已有的日记中不存在的条目，删除多余的条目；
     * 5）、如果AppendEntries#leaderCommit大于自身的当前commitIndex，
     * 则将commitIndex更新为Max(leaderCommit,commitIndex)，乐观
     * 地将本地已提交日记的commitIndex"跃进"到领导人为该Follower跟踪
     * 记得的值。【用于Follower刚从故障中恢复过来的场景。】
     *
     * @param appendEntries
     * @return
     */
    AppendEntriesResp appendCommand(AppendEntries appendEntries);

    /**
     * Leader通知Follower提交日记条目
     *
     * @param term  日记条目的term
     * @param index 日记条目的index
     */
    Boolean commit(Long term, Long index);

}
