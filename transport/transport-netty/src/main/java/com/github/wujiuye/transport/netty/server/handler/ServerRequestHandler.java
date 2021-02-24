package com.github.wujiuye.transport.netty.server.handler;

import com.github.wujiuye.transport.rpc.RpcInvokerRouter;
import com.github.wujiuye.transport.rpc.RpcMethod;
import com.github.wujiuye.transport.rpc.RpcRequest;
import com.github.wujiuye.transport.rpc.RpcResponse;
import com.github.wujiuye.transport.netty.protocol.message.rpc.RpcRequestMessage;
import com.github.wujiuye.transport.netty.protocol.message.rpc.RpcResponseMessage;
import com.github.wujiuye.transport.netty.commom.ProcessIdUtils;
import com.github.wujiuye.transport.netty.protocol.message.heartbeat.Heartbeat;
import com.github.wujiuye.transport.netty.server.connection.NettyConnectionPool;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

/**
 * 处理客户端请求的ChannelHandler
 *
 * @author wujiuye 2020/10/12
 */
public class ServerRequestHandler extends ChannelInboundHandlerAdapter {

    private final RpcInvokerRouter rpcInvokerRouter;
    private final NettyConnectionPool globalConnectionPool;

    public ServerRequestHandler(NettyConnectionPool globalConnectionPool,
                                RpcInvokerRouter rpcInvokerRouter) {
        this.globalConnectionPool = globalConnectionPool;
        this.rpcInvokerRouter = rpcInvokerRouter;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        globalConnectionPool.createConnection(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        globalConnectionPool.remove(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Heartbeat) {
            handlePingRequest(ctx, (Heartbeat) msg);
        } else if (msg instanceof RpcRequestMessage) {
            RpcRequestMessage rpcRequestMessage = (RpcRequestMessage) msg;
            RpcRequest rpcRequest = rpcRequestMessage.getRpcRequest();
            try {
                RpcMethod processor = rpcInvokerRouter.processor(rpcRequest.getInterfaces(),
                        rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                if (processor == null) {
                    throw new NullPointerException("404 not found.");
                }
                Object result = processor.invoke(rpcRequest.getArguments());
                writeProcessResponse(ctx, rpcRequestMessage, result);
            } catch (Throwable e) {
                writeProcessExceptionResponse(ctx, rpcRequestMessage, e);
            }
        }
    }

    private void writeProcessResponse(ChannelHandlerContext ctx, RpcRequestMessage nettyRequest, Object result) {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setResult(result);
        RpcResponseMessage response = new RpcResponseMessage(rpcResponse);
        response.setTransactionId(nettyRequest.getTransactionId());
        writeResponse(ctx, response);
    }

    private void writeProcessExceptionResponse(ChannelHandlerContext ctx, RpcRequestMessage nettyRequest, Throwable ex) {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setException(ex);
        RpcResponseMessage response = new RpcResponseMessage(rpcResponse);
        response.setTransactionId(nettyRequest.getTransactionId());
        writeResponse(ctx, response);
    }

    private void writeResponse(ChannelHandlerContext ctx, RpcResponseMessage response) {
        ctx.writeAndFlush(response);
    }

    private void handlePingRequest(ChannelHandlerContext ctx, Heartbeat heartbeat) {
        if (ctx.channel().attr(AttributeKey.valueOf("PID")).get() == null) {
            ctx.channel().attr(AttributeKey.valueOf("PID")).set(heartbeat.getPid());
        }
        // 响应服务端的pid
        heartbeat.setPid(ProcessIdUtils.getPid());
        ctx.writeAndFlush(heartbeat);
    }

    /**
     * 超时关闭连接
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            switch (((IdleStateEvent) evt).state()) {
                case WRITER_IDLE:
                case READER_IDLE:
                case ALL_IDLE:
                default:
                    // 关闭空闲连接
                    globalConnectionPool.remove(ctx.channel());
                    break;
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}
