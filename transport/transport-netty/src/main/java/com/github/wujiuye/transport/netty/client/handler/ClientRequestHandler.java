package com.github.wujiuye.transport.netty.client.handler;

import com.github.wujiuye.transport.netty.protocol.message.heartbeat.Heartbeat;
import com.github.wujiuye.transport.netty.client.ClientConstants;
import com.github.wujiuye.transport.netty.protocol.message.rpc.RpcResponseMessage;
import com.github.wujiuye.transport.netty.commom.NettyLogger;
import com.github.wujiuye.transport.netty.commom.ProcessIdUtils;
import com.github.wujiuye.transport.netty.client.syncreq.RequestPromiseHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 客户端发送请求的ChannelInboundHandler
 *
 * @author wujiuye 2020/10/12
 */
public class ClientRequestHandler extends ChannelInboundHandlerAdapter {

    private final AtomicInteger currentState;
    private final Runnable disconnectCallback;

    public ClientRequestHandler(AtomicInteger currentState, Runnable disconnectCallback) {
        this.currentState = currentState;
        this.disconnectCallback = disconnectCallback;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        currentState.set(ClientConstants.CLIENT_STATUS_STARTED);
        writeHearbeatResponse(ctx);
        NettyLogger.info("[ClientRequestHandler] Client handler active, remote address: " + getRemoteAddress(ctx));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Heartbeat) {
            handlePingResponse(ctx, (Heartbeat) msg);
        } else if (msg instanceof RpcResponseMessage) {
            RpcResponseMessage response = (RpcResponseMessage) msg;
            RequestPromiseHolder.completePromise(response.getTransactionId(), response);
        }
    }

    private void writeHearbeatResponse(ChannelHandlerContext ctx) {
        Heartbeat heartbeat = new Heartbeat();
        heartbeat.setPid(ProcessIdUtils.getPid());
        ctx.writeAndFlush(heartbeat);
    }

    private void handlePingResponse(ChannelHandlerContext ctx, Heartbeat heartbeat) {
        if (ctx.channel().attr(AttributeKey.valueOf("PID")).get() == null) {
            ctx.channel().attr(AttributeKey.valueOf("PID")).set(heartbeat.getPid());
        }
//        NettyLogger.debug("[ClientRequestHandler] Client ping OK (target server: {}，pid: {})",
//                getRemoteAddress(ctx), heartbeat.getPid());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        currentState.set(ClientConstants.CLIENT_STATUS_OFF);
        disconnectCallback.run();
    }

    private String getRemoteAddress(ChannelHandlerContext ctx) {
        if (ctx.channel().remoteAddress() == null) {
            return null;
        }
        InetSocketAddress inetAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        return inetAddress.getAddress().getHostAddress() + ":" + inetAddress.getPort();
    }

    public int getCurrentState() {
        return currentState.get();
    }

    public boolean hasStarted() {
        return getCurrentState() == ClientConstants.CLIENT_STATUS_STARTED;
    }

}
