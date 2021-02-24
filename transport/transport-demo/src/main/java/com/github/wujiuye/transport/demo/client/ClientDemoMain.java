package com.github.wujiuye.transport.demo.client;

import com.github.wujiuye.transport.connection.TransportIOException;
import com.github.wujiuye.transport.demo.service.HelloWordRpc;
import com.github.wujiuye.transport.rpc.RpcRequest;
import com.github.wujiuye.transport.rpc.RpcResponse;

/**
 * @author wujiuye 2021/01/07
 */
public class ClientDemoMain {

    public static void main(String[] args) throws TransportIOException, InterruptedException {
        TransportClientImpl transportClient = new TransportClientImpl();
        transportClient.restConnectToServer("127.0.0.1", 8080);
        Thread.sleep(1000);
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setInterfaces(HelloWordRpc.class);
        rpcRequest.setMethodName("sayHello");
        rpcRequest.setArguments(null);
        RpcResponse rpcResponse = transportClient.getClient().remoteInvoke(rpcRequest);
        System.out.println(rpcResponse);
    }

}
