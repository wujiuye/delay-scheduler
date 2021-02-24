package com.github.wujiuye.raft.sdk;

import com.github.wujiuye.raft.rpc.vote.RequestVote;
import com.github.wujiuye.raft.rpc.vote.RequestVoteResp;
import com.github.wujiuye.raft.rpc.vote.RequestVoteRpc;
import com.github.wujiuye.transport.connection.TransportIOException;
import com.github.wujiuye.transport.netty.client.NettyTransportClient;
import com.github.wujiuye.transport.rpc.RpcRequest;
import com.github.wujiuye.transport.rpc.RpcResponse;

/**
 * @author wujiuye 2020/12/17
 */
public class RequestVoteRpcImpl implements RequestVoteRpc {

    private NettyTransportClient client;

    public RequestVoteRpcImpl(NettyTransportClient client) {
        this.client = client;
    }

    @Override
    public RequestVoteResp requestVote(RequestVote requestVote) {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setInterfaces(RequestVoteRpc.class);
        rpcRequest.setMethodName("requestVote");
        rpcRequest.setParameterTypes(new Class<?>[]{RequestVote.class});
        rpcRequest.setArguments(new Object[]{requestVote});
        try {
            RpcResponse rpcResponse = client.remoteInvoke(rpcRequest);
            if (rpcResponse.getException() != null) {
                throw new RuntimeException(rpcResponse.getException());
            }
            return (RequestVoteResp) rpcResponse.getResult();
        } catch (TransportIOException e) {
            throw new RuntimeException(e);
        }
    }

}
