package com.github.wujiuye.raft.sdk;

import com.github.wujiuye.raft.CommandLogAppender;
import com.github.wujiuye.raft.Raft;
import com.github.wujiuye.raft.common.NodeIpPort;
import com.github.wujiuye.raft.rpc.replication.FollowerAppendEntriesRpc;
import com.github.wujiuye.raft.rpc.vote.ServerRequestVoteRpc;
import com.github.wujiuye.raft.StateMachine;
import com.github.wujiuye.transport.netty.server.NettyTransportServer;
import com.github.wujiuye.transport.rpc.RpcInvokerRouter;
import com.github.wujiuye.transport.server.config.ServiceConfig;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

/**
 * @author wujiuye 2020/12/17
 */
class RaftServer implements Closeable {

    private NettyTransportServer nettyTransportServer;
    private int curNodeId;
    private Set<NodeIpPort> nodes;
    private StateMachine stateMachine;
    private CommandLogAppender commandLogAppender;

    private void check() {
        if (stateMachine == null || commandLogAppender == null) {
            try {
                this.close();
            } catch (IOException ignored) {
            }
            throw new NullPointerException("stateMachine or commandLogAppender not config.");
        }
    }

    /**
     * @param curNodeId          当前节点ID
     * @param nodes              集群所有节点
     * @param stateMachine       状态机
     * @param commandLogAppender 日记写入器
     */
    public RaftServer(int curNodeId, Set<NodeIpPort> nodes,
                      StateMachine stateMachine, CommandLogAppender commandLogAppender) {
        this.curNodeId = curNodeId;
        this.nodes = nodes;
        this.stateMachine = stateMachine;
        this.commandLogAppender = commandLogAppender;
        this.initRaft();
    }

    /**
     * 初始化Raft
     */
    private void initRaft() {
        Raft.getRaftConfig().setId(curNodeId);
        Raft.init(nodes);
    }

    /**
     * 创建RPC路由器
     *
     * @param curNode 当前节点ip和端口信息
     * @return
     */
    private RpcInvokerRouter createRpcInvokerMapping(NodeIpPort curNode) {
        ServerRequestVoteRpc serverRequestVoteRpc
                = new ServerRequestVoteRpc(curNode.getNodeId(), commandLogAppender);
        FollowerAppendEntriesRpc followerAppendEntriesRpc
                = new FollowerAppendEntriesRpc(curNode.getNodeId(), commandLogAppender, stateMachine);
        return new RaftRpcInvokerRouter(followerAppendEntriesRpc, serverRequestVoteRpc);
    }

    /**
     * 启动Raft服务
     */
    public synchronized void startRaftServer() {
        check();
        NodeIpPort curNode = findNode(curNodeId, nodes);
        RpcInvokerRouter rpcInvokerRouter = createRpcInvokerMapping(curNode);
        NettyTransportServer nettyTransportServer = new NettyTransportServer(rpcInvokerRouter);
        nettyTransportServer.start(getServiceConfig(curNode));
        this.nettyTransportServer = nettyTransportServer;
    }

    private static NodeIpPort findNode(int nodeId, Set<NodeIpPort> nodes) {
        for (NodeIpPort nodeIpPort : nodes) {
            if (nodeIpPort.getNodeId() == nodeId) {
                return nodeIpPort;
            }
        }
        return null;
    }

    /**
     * 服务端连接配置
     *
     * @param curNode 当前节点ip和端口
     * @return
     */
    private static ServiceConfig getServiceConfig(NodeIpPort curNode) {
        ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setPort(curNode.getPort());
        serviceConfig.setWorkThreads(Runtime.getRuntime().availableProcessors());
        serviceConfig.setIdleTimeout(10);
        return serviceConfig;
    }

    @Override
    public void close() throws IOException {
        if (nettyTransportServer != null) {
            nettyTransportServer.stop();
        }
    }

}
