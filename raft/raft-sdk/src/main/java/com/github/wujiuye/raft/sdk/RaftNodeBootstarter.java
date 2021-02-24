package com.github.wujiuye.raft.sdk;

import com.github.wujiuye.raft.Raft;
import com.github.wujiuye.raft.common.SignalManager;
import com.github.wujiuye.raft.CommandLogAppender;
import com.github.wujiuye.raft.common.NodeIpPort;
import com.github.wujiuye.raft.RemoteRouter;
import com.github.wujiuye.raft.rpc.RaftCommandClient;
import com.github.wujiuye.raft.rpc.RaftVoteClient;
import com.github.wujiuye.raft.rpc.replication.AppendEntriesRpc;
import com.github.wujiuye.raft.rpc.vote.RequestVoteRpc;
import com.github.wujiuye.raft.StateMachine;

import java.io.IOException;
import java.util.Set;

/**
 * 引导启动类
 *
 * @author wujiuye 2020/12/16
 */
public final class RaftNodeBootstarter {

    /**
     * 引导启动
     *
     * @param curNodeId          当前节点ID
     * @param nodes              集群所有节点
     * @param stateMachine       状态机
     * @param commandLogAppender 日记Appeder
     */
    public static RaftCommandClient bootstartRaftNode(int curNodeId, Set<NodeIpPort> nodes,
                                                      StateMachine stateMachine, CommandLogAppender commandLogAppender) {
        // 启动服务端
        RaftServer raftServer = new RaftServer(curNodeId, nodes, stateMachine, commandLogAppender);
        raftServer.startRaftServer();

        // 启动客户端并连接到其它服务端
        RaftNodeClient raftNodeClient = new RaftNodeClient(curNodeId);
        raftNodeClient.restConnectToServer(nodes, Raft.getRaftConfig());

        // 处理请求
        RemoteRouter<AppendEntriesRpc> rpcRemoteRouter = new AppendEntriesRpcRemoteRouter(curNodeId, raftNodeClient);
        RaftCommandClient raftCommandClient = new RaftCommandClient(curNodeId, commandLogAppender, stateMachine, rpcRemoteRouter);

        // 选举
        RemoteRouter<RequestVoteRpc> remoteRouter = new RequestVoteRpcRemoteRouter(curNodeId, raftNodeClient);
        RaftVoteClient raftVoteClient = new RaftVoteClient(curNodeId, commandLogAppender, remoteRouter, raftCommandClient);

        // 注册kill钩子
        SignalManager.registToFirst(signal -> {
            raftNodeClient.close();
            try {
                raftServer.close();
            } catch (IOException ignored) {
            }
            try {
                raftCommandClient.close();
            } catch (IOException ignored) {
            }
            try {
                raftVoteClient.close();
            } catch (IOException ignored) {
            }

        });
        return raftCommandClient;
    }

}
