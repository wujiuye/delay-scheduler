package com.github.wujiuye.transport.netty.client.handler;

import com.github.wujiuye.transport.netty.protocol.message.heartbeat.Heartbeat;
import com.github.wujiuye.transport.netty.commom.ProcessIdUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 定时发送心跳包的处理器
 *
 * @author wujiuye 2020/10/12
 */
public class ClientIdlePingHandler extends ChannelInboundHandlerAdapter {

    /**
     * 发送心跳包
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
                    fireClientPing(ctx);
                    break;
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    private void fireClientPing(ChannelHandlerContext ctx) {
        Heartbeat packet = new Heartbeat();
        packet.setPid(ProcessIdUtils.getPid());
        ctx.writeAndFlush(packet);
    }

}
