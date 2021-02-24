package com.wujiuye.delayscheduler.client;

import com.github.wujiuye.transport.connection.TransportIOException;
import com.github.wujiuye.transport.rpc.RpcRequest;
import com.github.wujiuye.transport.rpc.RpcResponse;
import com.wujiuye.delayscheduler.core.ActionLog;
import com.wujiuye.delayscheduler.core.rpc.ActionRpcInterface;

import java.util.List;

/**
 * @author wujiuye 2021/01/20
 */
public class ActionRpcInterfaceClient implements ActionRpcInterface {

    private ClientsHolder clientsHolder;

    public ActionRpcInterfaceClient(ClientsHolder clientsHolder) {
        this.clientsHolder = clientsHolder;
    }

    @Override
    public Long submit(String application, ActionLog actionLog) {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setInterfaces(ActionRpcInterface.class);
        rpcRequest.setMethodName("submit");
        rpcRequest.setParameterTypes(new Class<?>[]{String.class, ActionLog.class});
        rpcRequest.setArguments(new Object[]{application, actionLog});
        try {
            RpcResponse rpcResponse = clientsHolder.remoteInvoke(rpcRequest);
            if (rpcResponse.getException() != null) {
                throw ExceptionUtils.warpThrowable(rpcResponse.getException());
            }
            return (long) rpcResponse.getResult();
        } catch (TransportIOException e) {
            throw ExceptionUtils.warpThrowable(e);
        }
    }

    @Override
    public Boolean cancel(String application, Long actionId) {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setInterfaces(ActionRpcInterface.class);
        rpcRequest.setMethodName("cancel");
        rpcRequest.setParameterTypes(new Class<?>[]{String.class, Long.class});
        rpcRequest.setArguments(new Object[]{application, actionId});
        try {
            RpcResponse rpcResponse = clientsHolder.remoteInvoke(rpcRequest);
            if (rpcResponse.getException() != null) {
                throw ExceptionUtils.warpThrowable(rpcResponse.getException());
            }
            return (boolean) rpcResponse.getResult();
        } catch (TransportIOException e) {
            throw ExceptionUtils.warpThrowable(e);
        }
    }

    @Override
    public List<ActionLog> pull(String application, Integer maxRecord) {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setInterfaces(ActionRpcInterface.class);
        rpcRequest.setMethodName("pull");
        rpcRequest.setParameterTypes(new Class<?>[]{String.class, Integer.class});
        rpcRequest.setArguments(new Object[]{application, maxRecord});
        try {
            RpcResponse rpcResponse = clientsHolder.remoteInvoke(rpcRequest);
            if (rpcResponse.getException() != null) {
                throw ExceptionUtils.warpThrowable(rpcResponse.getException());
            }
            return (List<ActionLog>) rpcResponse.getResult();
        } catch (TransportIOException e) {
            throw ExceptionUtils.warpThrowable(e);
        }
    }

    @Override
    public void commit(String application, Long actionId, Boolean success) {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setInterfaces(ActionRpcInterface.class);
        rpcRequest.setMethodName("commit");
        rpcRequest.setParameterTypes(new Class<?>[]{String.class, Long.class, Boolean.class});
        rpcRequest.setArguments(new Object[]{application, actionId, success});
        try {
            RpcResponse rpcResponse = clientsHolder.remoteInvoke(rpcRequest);
            if (rpcResponse.getException() != null) {
                throw ExceptionUtils.warpThrowable(rpcResponse.getException());
            }
        } catch (TransportIOException e) {
            throw ExceptionUtils.warpThrowable(e);
        }
    }

}
