package com.github.wujiuye.raft.rpc.vote;

import com.github.wujiuye.raft.*;
import com.github.wujiuye.raft.common.CountWaiter;
import com.github.wujiuye.raft.common.ElectionTimer;
import com.github.wujiuye.raft.common.IdUtils;
import com.github.wujiuye.raft.common.LoggerUtils;
import com.github.wujiuye.raft.CommandLog;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Follower选举方法
 *
 * @author wujiuye 2020/12/15
 */
public class ClientFollowerVoteFunction implements ElectionTimer.ElectionFunction, Closeable {

    private int nodeId;
    private RemoteRouter<RequestVoteRpc> remoteRouter;
    private CommandLogAppender commandLogAppender;
    private VoteListener voteListener;
    private ExecutorService voteExecutorService;

    private void initThreadPool() {
        voteExecutorService = Executors.newFixedThreadPool(
                Raft.nodeCount() - 1,
                r -> new Thread(r, "vote-rpc-" + IdUtils.newId()));
    }

    public ClientFollowerVoteFunction(int nodeId,
                                      RemoteRouter<RequestVoteRpc> remoteRouter,
                                      CommandLogAppender commandLogAppender,
                                      VoteListener voteListener) {
        this.nodeId = nodeId;
        this.remoteRouter = remoteRouter;
        this.commandLogAppender = commandLogAppender;
        this.voteListener = voteListener;
        this.initThreadPool();
    }

    @Override
    public void startElection() {
        // 1）将自己本地维护的当前任期号（current_term_id）加1。
        // 2）将自己的状态切换到候选人（Candidate），并为自己投票。也就是说每个候选人的第一张选票来自于他自己。
        // 3）向其所在集群中的其他节点发送RequestVoteRPC（RPC消息会携带“current_term_id”值），要求它们投票给自己。
        RaftNode raftNode = Raft.getNode(nodeId);
        voteListener.onVoteStart();
        if (!raftNode.restNewTermVote()) {
            return;
        }
        while (!voteExecutorService.isShutdown() && !voteExecutorService.isTerminated()) {
            if (raftNode.getRaftNodeRole() != RaftNodeRole.Candidate) {
                // 已经不是候选人了
                return;
            }
            RequestVote requestVote = new RequestVote();
            requestVote.setTerm(raftNode.getCurTerm());
            requestVote.setCandidateId(raftNode.getId());
            CommandLog commandLog = commandLogAppender.peek();
            if (commandLog != null) {
                requestVote.setLastLogTerm(commandLog.getTerm());
                requestVote.setLastLogIndex(commandLog.getIndex());
            } else {
                requestVote.setLastLogTerm(-1);
                requestVote.setLastLogIndex(-1);
            }
            // 开始拉票
            Set<RaftNode> nodes = Raft.getAllNodes();
            final CountWaiter countWaiter = new CountWaiter(Raft.nodeCount() - 1);
            final AtomicLong newLeaderTerm = new AtomicLong(0);
            for (RaftNode node : nodes) {
                if (node.getId().equals(raftNode.getId())) {
                    continue;
                }
                voteExecutorService.execute(() -> {
                    try {
                        RequestVoteResp resp = remoteRouter.routeRpc(node.getNodeIpPort()).requestVote(requestVote);
                        // 得到选票
                        if (resp.isVoteGranted()) {
                            raftNode.incVoteNumer();
                            countWaiter.countDownSuccess();
                            LoggerUtils.getLogger().info("===> vote response by node " + node.getId() + " , term " + resp.getTerm());
                        }
                        // 已经选举出Leader了
                        else if (resp.getTerm() > raftNode.getCurTerm()) {
                            newLeaderTerm.set(resp.getTerm());
                        }
                    } catch (Throwable ignored) {
                        countWaiter.countDownException();
                    } finally {
                        countWaiter.countDown();
                    }
                });
            }
            // 一个候选人有三种状态迁移的可能性:
            // 1）得到大多数节点的选票（包括自己），成为Leader。
            // 2）发现其他节点赢得了选举，主动切换回Follower。
            // 3）过了一段时间后，发现没有人赢得选举，重新发起一次选举。
            int count = Raft.nodeCount();
            int duoshuVote = (count / 2) + 1;
            // duoshuVote - 1: 算上自己给自己投的一票
            countWaiter.await(duoshuVote - 1);
            // 多数节点连接异常，继续当前期重新发起一次拉票
            if (countWaiter.exceptionCnt() > duoshuVote - 1) {
                countWaiter.await();
                continue;
            }
            if (raftNode.getRaftNodeRole() == RaftNodeRole.Candidate) {
                // 得到大多数节点的选票（包括自己），成为Leader
                if (raftNode.getVoteNumber() >= duoshuVote) {
                    LoggerUtils.getLogger().info("current node {} as Leader at term {}", nodeId, requestVote.getTerm());
                    Raft.getNode(nodeId).asLeader(requestVote.getTerm());
                    if (voteListener != null) {
                        voteListener.onVoteEnd(RaftNodeRole.Leader);
                    }
                }
                // 发现其他节点赢得了选举，主动切换回Follower。
                else if (newLeaderTerm.longValue() > 0) {
                    LoggerUtils.getLogger().info("current node {} as Follower at term {}", nodeId, newLeaderTerm.longValue());
                    Raft.getNode(nodeId).asFollower(newLeaderTerm.longValue());
                    if (voteListener != null) {
                        voteListener.onVoteEnd(RaftNodeRole.Follower);
                    }
                }
                // 如果是其它节点成为当前期的Leader，Leader会发送心跳包告知自己，要留给Leader足够时间发送心跳（选举超时大于心跳超时）
            }
            return;
        }
    }

    @Override
    public void close() throws IOException {
        voteExecutorService.shutdownNow();
    }

}
