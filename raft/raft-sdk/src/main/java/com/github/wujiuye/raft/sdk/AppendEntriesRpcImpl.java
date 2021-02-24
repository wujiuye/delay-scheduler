package com.github.wujiuye.raft.sdk;

import com.github.wujiuye.raft.rpc.replication.AppendEntries;
import com.github.wujiuye.raft.rpc.replication.AppendEntriesResp;
import com.github.wujiuye.raft.rpc.replication.AppendEntriesRpc;
import com.github.wujiuye.transport.connection.TransportIOException;
import com.github.wujiuye.transport.netty.client.NettyTransportClient;
import com.github.wujiuye.transport.rpc.RpcRequest;
import com.github.wujiuye.transport.rpc.RpcResponse;

/**
 * @author wujiuye 2020/12/17
 */
public class AppendEntriesRpcImpl implements AppendEntriesRpc {

    private NettyTransportClient client;

    public AppendEntriesRpcImpl(NettyTransportClient client) {
        this.client = client;
    }

    @Override
    public AppendEntriesResp appendCommand(AppendEntries appendEntries) {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setInterfaces(AppendEntriesRpc.class);
        rpcRequest.setMethodName("appendCommand");
        rpcRequest.setParameterTypes(new Class<?>[]{AppendEntries.class});
        rpcRequest.setArguments(new Object[]{appendEntries});
        try {
            RpcResponse rpcResponse = client.remoteInvoke(rpcRequest);
            if (rpcResponse.getException() != null) {
                throw new RuntimeException(rpcResponse.getException());
            }
            return (AppendEntriesResp) rpcResponse.getResult();
        } catch (TransportIOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean commit(Long term, Long index) {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setInterfaces(AppendEntriesRpc.class);
        rpcRequest.setMethodName("commit");
        rpcRequest.setParameterTypes(new Class<?>[]{Long.class, Long.class});
        rpcRequest.setArguments(new Object[]{term, index});
        try {
            RpcResponse rpcResponse = client.remoteInvoke(rpcRequest);
            if (rpcResponse.getException() != null) {
                throw new RuntimeException(rpcResponse.getException());
            }
            return (Boolean) rpcResponse.getResult();
        } catch (TransportIOException e) {
            throw new RuntimeException(e);
        }
    }

}
