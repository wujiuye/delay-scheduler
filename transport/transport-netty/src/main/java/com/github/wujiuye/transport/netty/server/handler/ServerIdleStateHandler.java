package com.github.wujiuye.transport.netty.server.handler;

import com.github.wujiuye.transport.netty.commom.NettyLogger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateHandler;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 空闲检测
 *
 * @author wujiuye 2020/10/12
 */
public class ServerIdleStateHandler extends IdleStateHandler {

    /**
     * @param timeout 读写超时
     */
    public ServerIdleStateHandler(int timeout) {
        super(0, 0, timeout, TimeUnit.SECONDS);
    }

    /**
     * 处理掉异常
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            NettyLogger.warn("exceptionCaught reset by peer ==> %s", cause.getMessage());
            return;
        }
        super.exceptionCaught(ctx, cause);
    }

}
