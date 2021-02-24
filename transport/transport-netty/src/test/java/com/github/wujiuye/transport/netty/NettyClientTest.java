package com.github.wujiuye.transport.netty;

import com.github.wujiuye.transport.client.TransportClient;
import com.github.wujiuye.transport.client.config.ClientConfig;
import com.github.wujiuye.transport.connection.TransportIOException;
import com.github.wujiuye.transport.netty.client.NettyTransportClient;
import com.github.wujiuye.transport.netty.rpc.RpcTest;
import com.github.wujiuye.transport.netty.commom.JsonUtils;
import com.github.wujiuye.transport.rpc.RpcRequest;
import com.github.wujiuye.transport.rpc.RpcResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NettyClientTest {

    private TransportClient transportClient;

    @Before
    public void init() throws Exception {
        transportClient = new NettyTransportClient();
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setHost("127.0.0.1");
        clientConfig.setPort(8090);
        clientConfig.setConnectionTimeout(1000);
        clientConfig.setRequestTimeout(1000);
        transportClient.start(clientConfig, 5000);
    }

    @Test
    public void testClient() throws InterruptedException {
        while (!transportClient.isReady()) {
            TimeUnit.SECONDS.sleep(1);
        }
        Thread.sleep(Integer.MAX_VALUE);
    }

    @Test
    public void testClient1() throws InterruptedException, TransportIOException {
        while (!transportClient.isReady()) {
            TimeUnit.SECONDS.sleep(1);
        }
        RpcRequest request = new RpcRequest();
        request.setInterfaces(RpcTest.class);
        request.setMethodName("sayHello");
        RpcResponse rpcResponse = transportClient.remoteInvoke(request);
        System.out.println(rpcResponse);
    }

    @Test
    public void testClient2() throws InterruptedException, TransportIOException {
        while (!transportClient.isReady()) {
            TimeUnit.SECONDS.sleep(1);
        }
        RpcRequest request = new RpcRequest();
        request.setInterfaces(RpcTest.class);
        request.setMethodName("sayHello");
        request.setParameterTypes(new Class<?>[]{String.class});
        RpcResponse rpcResponse = transportClient.remoteInvoke(request);
        System.out.println(rpcResponse);
        Map<String, Object> result = (Map<String, Object>) rpcResponse.getResult();
        System.out.println(JsonUtils.toJsonString(result));
    }

    @Test
    public void testClient3() throws InterruptedException, TransportIOException {
        while (!transportClient.isReady()) {
            TimeUnit.SECONDS.sleep(1);
        }
        RpcRequest request = new RpcRequest();
        request.setInterfaces(RpcTest.class);
        request.setMethodName("inc");
        request.setParameterTypes(new Class<?>[]{Integer.class, Integer.class});
        request.setArguments(new Object[]{1, 2});
        RpcResponse rpcResponse = transportClient.remoteInvoke(request);
        System.out.println(rpcResponse);
        int result = (int) rpcResponse.getResult();
        System.out.println(result);
    }

    @After
    public void des() throws Exception {
        transportClient.stop();
    }

}
