package com.github.wujiuye.raft.rpc;

import com.github.wujiuye.raft.*;
import com.github.wujiuye.raft.common.HeartbeatCallable;
import com.github.wujiuye.raft.common.HeartbeatScheduler;
import com.github.wujiuye.raft.common.LoggerUtils;
import com.github.wujiuye.raft.CommandLogAppender;
import com.github.wujiuye.raft.rpc.replication.AppendEntries;
import com.github.wujiuye.raft.rpc.replication.AppendEntriesRpc;
import com.github.wujiuye.raft.rpc.replication.LeaderAppendEntriesClient;
import com.github.wujiuye.raft.rpc.vote.VoteListener;

import java.io.Closeable;
import java.io.IOException;

/**
 * 负责处理外部应用的请求
 * 只有Leader节点可以接受请求，如果当前节点不是Leader节点，则响应主节点信息，让客户端重定向请求主节点
 *
 * @author wujiuye 2020/12/16
 */
public class RaftCommandClient implements VoteListener, Closeable {

    private int nodeId;
    private CommandCommit commandCommit;
    private CommandLogAppender commandLogAppender;
    private LeaderAppendEntriesClient leaderAppendEntriesClient;
    volatile private HeartbeatScheduler heartbeatScheduler;

    public RaftCommandClient(int nodeId, CommandLogAppender commandLogAppender, StateMachine stateMachine,
                             RemoteRouter<AppendEntriesRpc> remoteRouter) {
        this.nodeId = nodeId;
        this.commandCommit = new CommandCommit(commandLogAppender, stateMachine);
        this.commandLogAppender = commandLogAppender;
        this.leaderAppendEntriesClient = new LeaderAppendEntriesClient(nodeId, remoteRouter, commandLogAppender);
        this.seekCommitIndex(nodeId);
        this.initCurNodeTerm();
    }

    /**
     * 初始化当前节点期号
     */
    private void initCurNodeTerm() {
        CommandLog log = commandLogAppender.peek();
        long term = log == null ? 0 : log.getTerm();
        RaftNode raftNode = Raft.getNode(nodeId);
        if (raftNode != null) {
            raftNode.initCurTerm(term);
        }
    }

    /**
     * 初始化节点commitIndex
     */
    private void seekCommitIndex(int nodeId) {
        RaftNode raftNode = Raft.getNode(nodeId);
        if (raftNode != null) {
            raftNode.seekCommitIndex(commandCommit.maxCommitIndex());
        }
    }

    /**
     * 生成下一个logIndex
     *
     * @return
     */
    private synchronized long nextLogIndex() {
        long commintIndex = Raft.getNode(nodeId).curCommitIndex();
        return commintIndex + 1;
    }

    /**
     * 处理客户端请求
     *
     * @param command 序列化后的命令（由状态机解析执行）
     * @return true: 命令写日记成功，false: 失败
     */
    public synchronized CommandResp handleCommand(byte[] command) {
        CommandResp commandResp = new CommandResp();
        // 告诉客户端需要重定向到Leader节点发起请求
        if (Raft.getNode(nodeId).getRaftNodeRole() != RaftNodeRole.Leader) {
            commandResp.setSuccess(false);
            for (RaftNode raftNode : Raft.getAllNodes()) {
                if (raftNode.getRaftNodeRole() == RaftNodeRole.Leader) {
                    commandResp.setRedirectToLeader(raftNode.getNodeIpPort());
                    break;
                }
            }
            // 还未选举出Leader
            if (commandResp.getRedirectToLeader() == null) {
                throw new RuntimeException("the leader has not been elected");
            }
            return commandResp;
        }
        // 处理请求
        CommandLog commandLog = new CommandLog();
        commandLog.setCommand(command);
        commandLog.setTerm(Raft.getNode(nodeId).getCurTerm());
        commandLog.setIndex(nextLogIndex());
        commandLogAppender.append(commandLog);
        // 请求同步日记
        AppendEntries appendEntries = leaderAppendEntriesClient.newAppendEntries();
        appendEntries.setEntries(new CommandLog[]{commandLog});
        boolean success = leaderAppendEntriesClient.appendCommand(appendEntries);
        commandResp.setSuccess(success);
        if (success) {
            // 一旦日志项提交成功，Leader就将该日志条目对应的指令应用（apply）到本地状态机，并向客户端返回操作结果。
            commandCommit.commit(commandLog);
            Raft.getNode(nodeId).seekCommitIndex(commandLog.getIndex());
            leaderAppendEntriesClient.commit(commandLog);
        }
        return commandResp;
    }

    @Override
    public void close() throws IOException {
        LoggerUtils.getLogger().debug("raft command client close...");
        closeHeartbeat();
        leaderAppendEntriesClient.close();
    }

    @Override
    public void onVoteStart() {
        closeHeartbeat();
    }

    private void closeHeartbeat() {
        if (heartbeatScheduler != null) {
            heartbeatScheduler.stopHeartbeat();
            heartbeatScheduler = null;
        }
    }

    @Override
    public void onVoteEnd(RaftNodeRole raftNodeRole) {
        closeHeartbeat();
        // 当前节点变为Leader节点，开启定时发送心跳包
        if (raftNodeRole == RaftNodeRole.Leader) {
            heartbeatScheduler = new HeartbeatScheduler(Raft.getRaftConfig().getHeartbeatMs(),
                    () -> {
                        if (leaderAppendEntriesClient.sendHeartbeatCommand()) {
                            restHeartbeatTimer();
                        }
                    });
            heartbeatScheduler.startHeartbeat();
        }
    }

    /**
     * Follower能够在接收Leader心跳包时重置选举超时，而Leader只能是在发送心跳时重置选举超时
     * Leader停止发送心跳说明自身已经过期
     */
    private void restHeartbeatTimer() {
        try {
            HeartbeatCallable heartbeatCallable = Raft.getHeartbeatCallback();
            if (heartbeatCallable != null) {
                heartbeatCallable.onHeartbeat();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
