package com.github.wujiuye.raft.rpc.vote;

import com.github.wujiuye.raft.common.TermVoter;
import com.github.wujiuye.raft.Raft;
import com.github.wujiuye.raft.CommandLog;
import com.github.wujiuye.raft.appender.CommandLogAppender;
import com.github.wujiuye.raft.RaftNode;

/**
 * 拉票接收方实现
 *
 * @author wujiuye 2020/12/15
 */
public class ServerRequestVoteRpc implements RequestVoteRpc {

    private int nodeId;
    private CommandLogAppender commandLogAppender;

    public ServerRequestVoteRpc(int nodeId, CommandLogAppender commandLogAppender) {
        this.nodeId = nodeId;
        this.commandLogAppender = commandLogAppender;
    }

    @Override
    public RequestVoteResp requestVote(RequestVote requestVote) {
        RequestVoteResp resp = new RequestVoteResp();
        RaftNode raftNode = Raft.getNode(nodeId);
        // 1)、如果请求的term小于自身的当前term，返回false，提醒调用方term过时，并明确告诉调用方，这张选票不会投给它；
        long seftTerm = raftNode.getCurTerm();
        if (seftTerm > requestVote.getTerm()) {
            resp.setTerm(seftTerm);
            resp.setVoteGranted(false);
            return resp;
        }
        // seftTerm <= requestVote.getTerm()
        else {
            // 2）、如果请求的term大于自身的当前term，且如果之前没有把选票投给任何人（包括自己），则返回请求的term和true；
            TermVoter termVoter = raftNode.getTermVoterHolder().getTermVoter(requestVote.getTerm());
            if (termVoter.lockVote(nodeId)) {
                resp.setTerm(requestVote.getTerm());
                resp.setVoteGranted(true);
                return resp;
            }
            // 3）、否则，如果已经把选票投给了请求方，并且请求方的日记和自己的日记一样新，则返回请求的term和true；
            else if (requestVote.getCandidateId() == termVoter.getVoteNodeId()) {
                CommandLog commandLog = commandLogAppender.peek();
                // 请求方的日记是否和自己的日记一样新
                if (commandLog == null && requestVote.getLastLogIndex() == -1 && requestVote.getLastLogTerm() == -1) {
                    resp.setTerm(requestVote.getTerm());
                    resp.setVoteGranted(true);
                    return resp;
                }
                // 请求方的日记是否和自己的日记一样新
                if (commandLog != null && commandLog.getTerm() == requestVote.getLastLogTerm()
                        && commandLog.getIndex() == requestVote.getLastLogIndex()) {
                    resp.setTerm(requestVote.getTerm());
                    resp.setVoteGranted(true);
                    return resp;
                }
            }
            // 4）、否则，如果在此之前，已经把选票投给了其他人，则这张选票不能投给请求方，并明确告诉请求方，这张选票不会投给它；
            resp.setTerm(requestVote.getTerm());
            resp.setVoteGranted(false);
            return resp;
        }
    }

}
