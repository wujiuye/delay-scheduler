package com.github.wujiuye.transport.netty;

import com.github.wujiuye.transport.netty.rpc.RpcTest;
import com.github.wujiuye.transport.netty.rpc.RpcTestImpl;
import com.github.wujiuye.transport.netty.server.NettyTransportServer;
import com.github.wujiuye.transport.rpc.RpcInvokerRouter;
import com.github.wujiuye.transport.rpc.RpcMethod;
import com.github.wujiuye.transport.server.TransportServer;
import com.github.wujiuye.transport.server.config.ServiceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class NettyServerTest {

    private TransportServer transportServer;

    private final static RpcInvokerRouter RPC_INVOKER_ROUTER = new RpcInvokerRouter() {
        @Override
        public RpcMethod processor(Class<?> interfaces, String methodName, Class<?>[] parameterTypes) {
            if (interfaces == RpcTest.class) {
                RpcTest rpcTest = new RpcTestImpl();
                try {
                    Method method = RpcTest.class.getMethod(methodName, parameterTypes);
                    RpcMethod rpcMethod = new RpcMethod(rpcTest, method);
                    return rpcMethod;
                } catch (NoSuchMethodException e) {
                }
                return null;
            }
            return null;
        }
    };

    @Before
    public void init() throws Exception {
        transportServer = new NettyTransportServer(RPC_INVOKER_ROUTER);
        ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setPort(8090);
        serviceConfig.setWorkThreads(2);
        serviceConfig.setIdleTimeout(10);
        transportServer.start(serviceConfig);
    }

    @Test
    public void testServer() throws InterruptedException {
        while (!Thread.interrupted()) {
            TimeUnit.SECONDS.sleep(1);
            System.out.println("当前连接数：" + transportServer.getConnectionPool().count());
        }
    }

    @After
    public void des() throws Exception {
        transportServer.stop();
    }

}
