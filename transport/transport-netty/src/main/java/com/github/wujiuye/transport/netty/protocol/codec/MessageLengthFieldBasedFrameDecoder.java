package com.github.wujiuye.transport.netty.protocol.codec;

import com.github.wujiuye.transport.netty.protocol.message.MessageVersions;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * LengthFieldBasedFrameDecoder
 * 基于数据包长度固定的数据帧解码器，解决粘包问题
 *
 * @author wujiuye 2020/03/11
 */
public class MessageLengthFieldBasedFrameDecoder extends LengthFieldBasedFrameDecoder {

    /**
     * 存储数据段长度的四个字节在数据包中的偏移起始位置
     * (版本号+序列化算法+协议 = 3字节)
     */
    public static final int LENGTH_FIELD_OFFSET = MessageVersions.MAGIC_NUMBER.length + 3;
    /**
     * 存储数据段长度的四个字节
     */
    public static final int LENGTH_FIELD_LENGTH = 4;

    public MessageLengthFieldBasedFrameDecoder() {
        super(Integer.MAX_VALUE, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH);
    }

    /**
     * 验证数据包，即是否是以魔数MAGIC_NUMBER开始的
     *
     * @param ctx
     * @param in
     * @return
     * @throws Exception
     */
    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame != null) {
            byte[] flags = new byte[MessageVersions.MAGIC_NUMBER.length];
            frame.readBytes(flags);
            frame.resetReaderIndex();
            if (flags[0] != MessageVersions.MAGIC_NUMBER[0] || flags[1] != MessageVersions.MAGIC_NUMBER[1]) {
                return null;
            }
        }
        return frame;
    }

}
