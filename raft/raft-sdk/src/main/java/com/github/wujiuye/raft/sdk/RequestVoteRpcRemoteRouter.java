package com.github.wujiuye.raft.sdk;

import com.github.wujiuye.raft.common.NodeIpPort;
import com.github.wujiuye.raft.RemoteRouter;
import com.github.wujiuye.raft.rpc.vote.RequestVoteRpc;

/**
 * @author wujiuye 2020/12/17
 */
public class RequestVoteRpcRemoteRouter implements RemoteRouter<RequestVoteRpc> {

    private final int curNodeId;
    private RaftNodeClient raftNodeClient;

    public RequestVoteRpcRemoteRouter(int curNodeId, RaftNodeClient raftNodeClient) {
        this.curNodeId = curNodeId;
        this.raftNodeClient = raftNodeClient;
    }

    @Override
    public RequestVoteRpc routeRpc(NodeIpPort toNode) {
        if (toNode.getNodeId() == curNodeId) {
            throw new RuntimeException("not supper rpc invoke self.");
        }
        return new RequestVoteRpcImpl(raftNodeClient.getClient(toNode.getNodeId()));
    }

}
