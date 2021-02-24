package com.github.wujiuye.raft.rpc;

import com.github.wujiuye.raft.CommandLogAppender;
import com.github.wujiuye.raft.Raft;
import com.github.wujiuye.raft.common.ElectionTimer;
import com.github.wujiuye.raft.common.HeartbeatCallable;
import com.github.wujiuye.raft.RemoteRouter;
import com.github.wujiuye.raft.rpc.vote.ClientFollowerVoteFunction;
import com.github.wujiuye.raft.rpc.vote.RequestVoteRpc;
import com.github.wujiuye.raft.rpc.vote.VoteListener;

import java.io.Closeable;
import java.io.IOException;

/**
 * Raft选举使用的场景类
 *
 * @author wujiuye 2020/12/16
 */
public class RaftVoteClient implements HeartbeatCallable, Closeable {

    private int nodeId;
    private CommandLogAppender commandLogAppender;
    private RemoteRouter<RequestVoteRpc> requestVoteRpcRemoteRouter;
    private ElectionTimer electionTimer;
    private ClientFollowerVoteFunction clientFollowerVoteFunction;

    public RaftVoteClient(int nodeId, CommandLogAppender commandLogAppender,
                          RemoteRouter<RequestVoteRpc> requestVoteRpcRemoteRouter,
                          VoteListener voteListener) {
        this.nodeId = nodeId;
        this.commandLogAppender = commandLogAppender;
        this.requestVoteRpcRemoteRouter = requestVoteRpcRemoteRouter;
        this.init(voteListener);
        Raft.holdHeartbeatCallable(this);
    }

    /**
     * 创建选举方法、创建选举定时器
     *
     * @param voteListener 选举结果监听器
     */
    private void init(VoteListener voteListener) {
        this.clientFollowerVoteFunction = new ClientFollowerVoteFunction(nodeId, requestVoteRpcRemoteRouter,
                this.commandLogAppender, voteListener);
        this.electionTimer = new ElectionTimer(Raft.getRaftConfig().getElectionMs(),
                this.clientFollowerVoteFunction);
        this.electionTimer.startTimer();
    }

    @Override
    public void close() throws IOException {
        this.electionTimer.stopTimer();
        this.clientFollowerVoteFunction.close();
    }

    /**
     * 在接收到心跳包时回调调用
     *
     * @return
     */
    @Override
    public void onHeartbeat() {
        electionTimer.resetTimer();
    }

}
