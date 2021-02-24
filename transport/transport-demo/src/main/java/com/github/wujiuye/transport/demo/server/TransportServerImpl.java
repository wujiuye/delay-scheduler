package com.github.wujiuye.transport.demo.server;

import com.github.wujiuye.transport.netty.server.NettyTransportServer;
import com.github.wujiuye.transport.rpc.RpcInvokerRouter;
import com.github.wujiuye.transport.server.config.ServiceConfig;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author wujiuye 2020/12/17
 */
public class TransportServerImpl {

    private final static AtomicReference<NettyTransportServer> NETTY_TRANSPORT_SERVER = new AtomicReference<>(null);

    private static ServiceConfig getServiceConfig(int port) {
        ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setPort(port);
        serviceConfig.setWorkThreads(Runtime.getRuntime().availableProcessors());
        serviceConfig.setIdleTimeout(10);
        return serviceConfig;
    }

    public static synchronized void startServer(int port, RpcInvokerRouter rpcInvokerRouter) {
        if (NETTY_TRANSPORT_SERVER.get() != null) {
            return;
        }
        NettyTransportServer nettyTransportServer = new NettyTransportServer(rpcInvokerRouter);
        nettyTransportServer.start(getServiceConfig(port));
        NETTY_TRANSPORT_SERVER.set(nettyTransportServer);
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (NETTY_TRANSPORT_SERVER.get() != null) {
                NETTY_TRANSPORT_SERVER.get().stop();
                NETTY_TRANSPORT_SERVER.set(null);
            }
        }));
    }

}
