package com.github.wujiuye.transport.netty.server;

import com.github.wujiuye.transport.netty.server.handler.ServerRequestHandler;
import com.github.wujiuye.transport.rpc.RpcInvokerRouter;
import com.github.wujiuye.transport.netty.protocol.codec.MessageDecoder;
import com.github.wujiuye.transport.netty.protocol.codec.MessageEncoder;
import com.github.wujiuye.transport.netty.protocol.codec.MessageLengthFieldBasedFrameDecoder;
import com.github.wujiuye.transport.netty.commom.NettyLogger;
import com.github.wujiuye.transport.server.TransportServer;
import com.github.wujiuye.transport.server.config.ServiceConfig;
import com.github.wujiuye.transport.server.connection.ConnectionPool;
import com.github.wujiuye.transport.netty.server.connection.NettyConnectionPool;
import com.github.wujiuye.transport.netty.server.handler.ServerIdleStateHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Netty实现的服务端
 *
 * @author wujiuye 2020/10/12
 */
public class NettyTransportServer implements TransportServer {

    private static final int MAX_RETRY_TIMES = 3;
    private static final int RETRY_SLEEP_MS = 2000;

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    private final NettyConnectionPool connectionPool = new NettyConnectionPool();

    private final AtomicInteger currentState = new AtomicInteger(ServerConstants.SERVER_STATUS_OFF);
    private final AtomicInteger failedTimes = new AtomicInteger(0);

    private ServiceConfig serviceConfig;

    @Override
    public void start(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
        this.start();
    }

    @Override
    public ConnectionPool getConnectionPool() {
        return this.connectionPool;
    }

    private RpcInvokerRouter rpcInvokerRouter;

    public NettyTransportServer(RpcInvokerRouter rpcInvokerRouter) {
        this.rpcInvokerRouter = rpcInvokerRouter;
    }

    private void start() {
        if (!currentState.compareAndSet(ServerConstants.SERVER_STATUS_OFF,
                ServerConstants.SERVER_STATUS_STARTING)) {
            return;
        }
        ServerBootstrap b = new ServerBootstrap();
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup(serviceConfig.getWorkThreads());
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new MessageLengthFieldBasedFrameDecoder());
                        p.addLast(new MessageDecoder());
                        p.addLast(new MessageEncoder());
                        p.addLast(new ServerIdleStateHandler(serviceConfig.getIdleTimeout()));
                        p.addLast(new ServerRequestHandler(connectionPool, rpcInvokerRouter));
                    }
                })
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.SO_SNDBUF, 32 * 1024)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .childOption(ChannelOption.SO_TIMEOUT, 10)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_RCVBUF, 32 * 1024);
        b.bind(serviceConfig.getPort())
                .addListener((GenericFutureListener<ChannelFuture>) future -> {
                    if (future.cause() != null) {
                        NettyLogger.info("[NettyClusterTransportServer] server start failed (port=" + serviceConfig.getPort() + "), failedTimes: " + failedTimes.get(),
                                future.cause());
                        currentState.compareAndSet(ServerConstants.SERVER_STATUS_STARTING, ServerConstants.SERVER_STATUS_OFF);
                        int failCount = failedTimes.incrementAndGet();
                        if (failCount > MAX_RETRY_TIMES) {
                            return;
                        }
                        try {
                            Thread.sleep(failCount * RETRY_SLEEP_MS);
                            start();
                        } catch (Throwable e) {
                            NettyLogger.info("[NettyClusterTransportServer] Failed to start server when retrying", e);
                        }
                    } else {
                        NettyLogger.info("[NettyClusterTransportServer]  server started success at port " + serviceConfig.getPort());
                        currentState.compareAndSet(ServerConstants.SERVER_STATUS_STARTING, ServerConstants.SERVER_STATUS_STARTED);
                    }
                });
    }

    @Override
    public void stop() {
        while (currentState.get() == ServerConstants.SERVER_STATUS_STARTING) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
        if (currentState.compareAndSet(ServerConstants.SERVER_STATUS_STARTED, ServerConstants.SERVER_STATUS_OFF)) {
            try {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
                connectionPool.shutdownAll();
                failedTimes.set(0);
                NettyLogger.info("[NettyClusterTransportServer] Cluster transport server stopped");
            } catch (Exception ex) {
                NettyLogger.warn("[NettyClusterTransportServer] Failed to stop server (port=" + serviceConfig.getPort() + ")", ex);
            }
        }
    }

}
