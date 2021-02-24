package com.github.wujiuye.raft.sdk;

import com.github.wujiuye.raft.common.NodeIpPort;
import com.github.wujiuye.raft.RemoteRouter;
import com.github.wujiuye.raft.rpc.replication.AppendEntriesRpc;

/**
 * @author wujiuye 2020/12/17
 */
public class AppendEntriesRpcRemoteRouter implements RemoteRouter<AppendEntriesRpc> {

    private final int curNodeId;
    private RaftNodeClient raftNodeClient;

    public AppendEntriesRpcRemoteRouter(int curNodeId, RaftNodeClient raftNodeClient) {
        this.curNodeId = curNodeId;
        this.raftNodeClient = raftNodeClient;
    }

    @Override
    public AppendEntriesRpc routeRpc(NodeIpPort toNode) {
        if (toNode.getNodeId() == curNodeId) {
            throw new RuntimeException("not supper rpc invoke self.");
        }
        return new AppendEntriesRpcImpl(raftNodeClient.getClient(toNode.getNodeId()));
    }

}
