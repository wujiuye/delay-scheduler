package com.github.wujiuye.raft.rpc.vote;

/**
 * 拉票RPC
 *
 * @author wujiuye 2020/12/15
 */
public interface RequestVoteRpc {

    /**
     * 发起拉票请求
     * 请求接收方实现：
     * 1)、如果请求的term小于自身的当前term，返回false，提醒调用方
     * term过时，并明确告诉调用方，这张选票不会投给它；
     * 2）、如果请求的term大于自身的当前term，且如果之前没有把选票投
     * 给任何人（包括自己），则返回请求的term和true；
     * 3）、否则，如果已经把选票投给了请求方，并且请求方的日记和
     * 自己的日记一样新，则返回请求的term和true；
     * @see RequestVote#lastLogTerm
     * @see RequestVote#lastLogIndex
     * 4）、否则，如果在此之前，已经把选票投给了其他人，则这张
     * 选票不能投给请求方，并明确告诉请求方，这张选票不会投给它；
     *
     * @param requestVote 拉票请求data
     * @return 拉票响应data
     */
    RequestVoteResp requestVote(RequestVote requestVote);

}
