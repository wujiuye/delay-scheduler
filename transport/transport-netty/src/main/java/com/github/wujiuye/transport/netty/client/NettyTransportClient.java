package com.github.wujiuye.transport.netty.client;

import com.github.wujiuye.transport.connection.ConnectionTimeoutException;
import com.github.wujiuye.transport.connection.WaitResponseTimeoutException;
import com.github.wujiuye.transport.netty.client.handler.ClientIdlePingHandler;
import com.github.wujiuye.transport.netty.client.handler.ClientIdleStateHandler;
import com.github.wujiuye.transport.netty.client.handler.ClientRequestHandler;
import com.github.wujiuye.transport.netty.protocol.codec.MessageDecoder;
import com.github.wujiuye.transport.netty.protocol.codec.MessageEncoder;
import com.github.wujiuye.transport.netty.protocol.codec.MessageLengthFieldBasedFrameDecoder;
import com.github.wujiuye.transport.netty.protocol.message.rpc.RpcRequestMessage;
import com.github.wujiuye.transport.netty.protocol.message.rpc.RpcResponseMessage;
import com.github.wujiuye.transport.rpc.RpcRequest;
import com.github.wujiuye.transport.rpc.RpcResponse;
import com.github.wujiuye.transport.netty.protocol.message.Message;
import com.github.wujiuye.transport.netty.commom.NettyLogger;
import com.github.wujiuye.transport.client.config.ClientConfig;
import com.github.wujiuye.transport.client.TransportClient;
import com.github.wujiuye.transport.client.connection.ConnectionListener;
import com.github.wujiuye.transport.client.connection.ConnectionListenerProvider;
import com.github.wujiuye.transport.netty.client.connection.NettyConnection;
import com.github.wujiuye.transport.connection.TransportIOException;
import com.github.wujiuye.transport.netty.client.syncreq.RequestIdGenerator;
import com.github.wujiuye.transport.netty.client.syncreq.RequestPromiseHolder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.AbstractMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Netty实现的客户端
 *
 * @author wujiuye 2020/10/12
 */
public class NettyTransportClient implements TransportClient {

    private static final ScheduledExecutorService SCHEDULER = new ScheduledThreadPoolExecutor(1,
            new ThreadFactory() {
                private final AtomicInteger index = new AtomicInteger();

                @Override
                public Thread newThread(Runnable runnable) {
                    Thread thread = new Thread(runnable, "transport-client-scheduler-" + index.getAndIncrement());
                    thread.setDaemon(true);
                    return thread;
                }
            });

    private NettyConnection connection;
    private NioEventLoopGroup eventLoopGroup;

    private final AtomicInteger currentState = new AtomicInteger(ClientConstants.CLIENT_STATUS_OFF);

    private ClientRequestHandler clientHandler;
    private final AtomicBoolean shouldRetry = new AtomicBoolean(true);
    private ClientConfig clientConfig;
    private final AtomicLong failRetryConnectionMs = new AtomicLong(0);

    private Bootstrap initClientBootstrap() {
        Bootstrap bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) clientConfig.getConnectionTimeout())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        clientHandler = new ClientRequestHandler(currentState, disconnectCallback);
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new MessageLengthFieldBasedFrameDecoder());
                        pipeline.addLast(new MessageDecoder());
                        pipeline.addLast(new MessageEncoder());
                        pipeline.addLast(new ClientIdleStateHandler(5));
                        pipeline.addLast(new ClientIdlePingHandler());
                        pipeline.addLast(clientHandler);
                    }
                });
        return bootstrap;
    }

    private void connect(Bootstrap b) {
        if (currentState.compareAndSet(ClientConstants.CLIENT_STATUS_OFF, ClientConstants.CLIENT_STATUS_PENDING)) {
            b.connect(clientConfig.getHost(), clientConfig.getPort())
                    .addListener((GenericFutureListener<ChannelFuture>) future -> {
                        if (future.cause() != null) {
//                            NettyLogger.debug(
//                                    String.format("[NettyClusterTransportClient] Could not connect to <%s:%d> after %d times",
//                                            clientConfig.getHost(), clientConfig.getPort(),
//                                            failRetryConnectionMs.get()), future.cause());
                            try {
                                if (connection != null) {
                                    connection.close();
                                }
                            } catch (Throwable ignored) {
                            }
                            connection = null;
                            List<ConnectionListener> connectionListeners
                                    = ConnectionListenerProvider.getInstance().allConnectionFailListener();
                            if (connectionListeners != null && !connectionListeners.isEmpty()) {
                                for (ConnectionListener connectionListener : connectionListeners) {
                                    connectionListener.onConnectTimeout(clientConfig.getHost(), clientConfig.getPort());
                                }
                            }
                        } else {
                            connection = new NettyConnection(future.channel());
//                            NettyLogger.info(
//                                    "[NettyClusterTransportClient] Successfully connect to server <"
//                                            + clientConfig.getHost() + ":" + clientConfig.getPort() + ">");
                        }
                    });
        }
    }

    /**
     * 销毁连接回调，重试重新连接
     */
    private Runnable disconnectCallback = new Runnable() {
        @Override
        public void run() {
            if (!shouldRetry.get()) {
                return;
            }
            SCHEDULER.schedule(() -> {
                if (shouldRetry.get()) {
//                    NettyLogger.info("[NettyClusterTransportClient] Reconnecting to server <"
//                            + clientConfig.getHost() + ":" + clientConfig.getPort() + ">");
                    try {
                        startInternal();
                    } catch (Exception e) {
//                        NettyLogger.warn("[NettyClusterTransportClient] Failed to reconnect to server", e);
                    }
                }
            }, failRetryConnectionMs.get(), TimeUnit.MILLISECONDS);
            cleanUp();
        }
    };

    @Override
    public ClientConfig getConfig() {
        return this.clientConfig;
    }

    @Override
    public void start(ClientConfig clientConfig, long failRetryConnectionMs) {
        this.failRetryConnectionMs.set(failRetryConnectionMs);
        this.clientConfig = clientConfig;
        shouldRetry.set(true);
        startInternal();
    }

    private void startInternal() {
        connect(initClientBootstrap());
    }

    private void cleanUp() {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception ignored) {
            }
            connection = null;
        }
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() throws Exception {
        shouldRetry.set(false);
        while (currentState.get() == ClientConstants.CLIENT_STATUS_PENDING) {
            try {
                Thread.sleep(100);
            } catch (Exception ignored) {
            }
        }
        cleanUp();
        NettyLogger.info("[NettyClusterTransportClient] Cluster transport client stopped");
    }

    @Override
    public RpcResponse remoteInvoke(RpcRequest request) throws TransportIOException {
        if (!isReady()) {
            throw new ConnectionTimeoutException("连接异常");
        }
        String tid = RequestIdGenerator.generatorRequestId();
        try {
            RpcRequestMessage rpcRequestMessage = new RpcRequestMessage(request);
            rpcRequestMessage.setTransactionId(tid);
            connection.getChannel().writeAndFlush(rpcRequestMessage);
            ChannelPromise promise = connection.getChannel().newPromise();
            RequestPromiseHolder.putPromise(tid, promise);
            if (!promise.await(clientConfig.getRequestTimeout())) {
                throw new WaitResponseTimeoutException("等待超时");
            }
            AbstractMap.SimpleEntry<ChannelPromise, Message> entry = RequestPromiseHolder.getEntry(tid);
            if (entry == null || entry.getValue() == null) {
                throw new WaitResponseTimeoutException("未获取到响应");
            }
            return ((RpcResponseMessage) entry.getValue()).getRpcResponse();
        } catch (InterruptedException e) {
            throw new WaitResponseTimeoutException("等待超时");
        } finally {
            RequestPromiseHolder.remove(tid);
        }
    }

    @Override
    public boolean isReady() {
        return connection != null && clientHandler != null && clientHandler.hasStarted();
    }

}
