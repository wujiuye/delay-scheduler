package com.github.wujiuye.raft.rpc.replication;

import com.github.wujiuye.raft.*;
import com.github.wujiuye.raft.common.HeartbeatCallable;
import com.github.wujiuye.raft.appender.CommandLogAppender;
import com.github.wujiuye.raft.common.LoggerUtils;

/**
 * Follower接收Leader的请求
 *
 * @author wujiuye 2020/12/15
 */
public class FollowerAppendEntriesRpc implements AppendEntriesRpc {

    private int nodeId;
    private CommandCommit commandCommit;
    private CommandLogAppender commandLogAppender;

    public FollowerAppendEntriesRpc(int nodeId, CommandLogAppender commandLogAppender, StateMachine stateMachine) {
        this.nodeId = nodeId;
        this.commandLogAppender = commandLogAppender;
        this.commandCommit = new CommandCommit(commandLogAppender, stateMachine);
    }

    /**
     * 接收到AppendEntries Rpc请求，回调心跳监听器
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

    /**
     * 接收到Leader的心跳，说明自己已经是Follower
     *
     * @param leaderNodeId Leader节点ID
     */
    private void handleVoteResult(int leaderNodeId, long leaderTerm) {
        Raft.getNode(leaderNodeId).asLeader(leaderTerm);
    }

    @Override
    public AppendEntriesResp appendCommand(AppendEntries appendEntries) {
        restHeartbeatTimer();
        RaftNode raftNode = Raft.getNode(nodeId);
        AppendEntriesResp resp;
        // 1）、如果自身的term大于AppendEntries#term，另领导人的任期号小于自身，则返回自身的term，且success为false；
        if (raftNode.getCurTerm() > appendEntries.getTerm()
                && Raft.getNode(appendEntries.getLeaderId()).getCurTerm() < raftNode.getCurTerm()) {
            resp = new AppendEntriesResp(raftNode.getCurTerm(), false);
            LoggerUtils.getLogger().debug("step-1 {},{}", appendEntries, resp);
            return resp;
        }
        // 2）、否则，如果Follower自身在prevLogIndex日记的任期号与prevLogTerm不匹配，返回自身的term，且success为false；
        CommandLog commandLog = commandLogAppender.index(appendEntries.getPrevLogIndex());
        if ((appendEntries.getPrevLogIndex() >= 0 && commandLog == null)
                || (commandLog != null && commandLog.getTerm() != appendEntries.getPrevLogTerm())) {
            resp = new AppendEntriesResp(raftNode.getCurTerm(), false);
            if (appendEntries.getEntries() != null) {
                LoggerUtils.getLogger().debug("step-2 prevLogIndex={},{},{},{}",
                        appendEntries.getPrevLogIndex(), commandLog, appendEntries, resp);
            }
            return resp;
        }
        // 如果这只是一个心跳包
        if (appendEntries.getEntries() == null || appendEntries.getEntries().length == 0) {
            handleVoteResult(appendEntries.getLeaderId(), appendEntries.getTerm());
            resp = new AppendEntriesResp(appendEntries.getTerm(), true);
            return resp;
        }
        CommandLog[] entities = appendEntries.getEntries();
        int notEsixtStartIndex = 0;
        // 3）、否则，Follower进行日记一致性检查；
        for (int i = 0; i < entities.length; i++) {
            CommandLog log = entities[i];
            commandLog = commandLogAppender.index(log.getIndex());
            // 删除已经存在，但不一致的日记
            if (commandLog != null && commandLog.getTerm() != log.getTerm()) {
                commandLogAppender.removeRange(log.getTerm(), log.getIndex());
                notEsixtStartIndex = i + 1;
                break;
            }
        }
        // 4）、添加任何在已有的日记中不存在的条目，删除多余的条目；
        for (; notEsixtStartIndex < entities.length; notEsixtStartIndex++) {
            // 添加不存在的条目
            commandLogAppender.append(entities[notEsixtStartIndex]);
            // ==============================================
            // 如果是复制已经提交的条目，复制成功时直接提交
            CommandLog copyCommandLog = entities[notEsixtStartIndex];
            if (copyCommandLog.getStatus() == 1) {
                this.commit(copyCommandLog.getTerm(), copyCommandLog.getIndex());
            }
            // ==============================================
        }
        resp = new AppendEntriesResp(appendEntries.getTerm(), true);
        // 5）、如果AppendEntries#leaderCommit大于自身的当前commitIndex，则将commitIndex更新为Max(leaderCommit,commitIndex)，乐观
        // 地将本地已提交日记的commitIndex"跃进"到领导人为该Follower跟踪记得的值。用于Follower刚从故障中恢复过来的场景。
        if (appendEntries.getLeaderCommit() > raftNode.curCommitIndex()) {
            raftNode.seekCommitIndex(appendEntries.getLeaderCommit());
        }
        return resp;
    }

    @Override
    public Boolean commit(Long term, Long index) {
        CommandLog commandLog = commandLogAppender.index(index);
        // 跟随Leader提交日记条目
        commandCommit.commit(commandLog);
        // 设置最新的commitIndex
        Raft.getNode(nodeId).seekCommitIndex(index);
        return true;
    }

}
