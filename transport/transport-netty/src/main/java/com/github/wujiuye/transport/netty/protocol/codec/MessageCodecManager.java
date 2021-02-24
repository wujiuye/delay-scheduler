package com.github.wujiuye.transport.netty.protocol.codec;

import com.github.wujiuye.transport.netty.protocol.message.heartbeat.Heartbeat;
import com.github.wujiuye.transport.netty.protocol.serialize.JsonSerializer;
import com.github.wujiuye.transport.netty.protocol.serialize.Serializer;
import com.github.wujiuye.transport.netty.protocol.serialize.SerializerEnum;
import com.github.wujiuye.transport.netty.protocol.message.Message;
import com.github.wujiuye.transport.netty.protocol.message.MessageMetate;
import com.github.wujiuye.transport.netty.protocol.message.MessageTypes;
import com.github.wujiuye.transport.netty.protocol.message.MessageVersions;
import com.github.wujiuye.transport.netty.protocol.message.rpc.RpcRequestMessage;
import com.github.wujiuye.transport.netty.protocol.message.rpc.RpcResponseMessage;
import io.netty.buffer.ByteBuf;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据包编解码实现类
 *
 * @author wujiuye 2020/10/13
 * }
 */
public class MessageCodecManager {

    private final static Map<Byte, MessageMetate> MESSAGE_TYPE_PACKET_MAP = new HashMap<>();
    private final static Map<Byte, Serializer> SERIALIZER_MAP = new HashMap<>();

    /**
     * 注册默认序列化算法
     */
    static {
        Serializer jsonSerialize = new JsonSerializer();
        SERIALIZER_MAP.put(jsonSerialize.getSerializer(), jsonSerialize);
    }

    /**
     * 注册默认数据包
     */
    static {
        regist(MessageTypes.MSG_TYPE_PING, MessageVersions.VERSION, Heartbeat.class);
        regist(MessageTypes.MSG_TYPE_RPC_REQUEST, MessageVersions.VERSION, RpcRequestMessage.class);
        regist(MessageTypes.MSG_TYPE_RPC_RESPONSE, MessageVersions.VERSION, RpcResponseMessage.class);
    }

    private MessageCodecManager() {
    }

    /**
     * 注册数据包类型
     *
     * @param
     * @param type        消息类型
     * @param version     当前数据包使用的版本
     * @param packetClass 数据包类型
     */
    public static void regist(Byte type, Byte version, Class<? extends Message> packetClass) {
        if (type != null && packetClass != null) {
            MessageMetate metate = new MessageMetate(version, packetClass);
            MESSAGE_TYPE_PACKET_MAP.put(type, metate);
        }
    }

    /**
     * 数据包编码
     *
     * @param byteBuf
     * @param message        数据包
     * @param serializerCode 序列化算法
     */
    public static void encode(ByteBuf byteBuf, Message message, SerializerEnum serializerCode) {
        // 魔术+一个字节的版本号+一个字节的序列化算法+一个字节的指令+四个字节的数据长度+实际数据
        byteBuf.writeBytes(MessageVersions.MAGIC_NUMBER);
        // 写入版本号
        byteBuf.writeByte(message.getVersion());

        // 序列化
        Serializer serializer = getSerializer(serializerCode.getValue());
        // 写入序列化算法
        byteBuf.writeByte(serializer.getSerializer());

        // 序列化 java 对象
        byte[] bytes = serializer.serialize(message);
        // 对消息内容加密
        bytes = Base64.getEncoder().encode(bytes);

        // 写入消息类型
        byteBuf.writeByte(message.getType());
        // 写入序列化数据的长度
        byteBuf.writeInt(bytes.length);
        // 写入序列化数据
        byteBuf.writeBytes(bytes);
    }


    /**
     * 解码为数据包
     *
     * @param byteBuf
     * @return
     */
    public static Message decode(ByteBuf byteBuf) {
        // 跳过 magic number
        byteBuf.skipBytes(MessageVersions.MAGIC_NUMBER.length);
        // 读取版本号
        byte version = byteBuf.readByte();
        // 序列化算法
        byte serializerCode = byteBuf.readByte();
        // 消息类型
        byte type = byteBuf.readByte();
        // body长度
        int length = byteBuf.readInt();
        if (length > 0) {
            byte[] bytes = new byte[length];
            byteBuf.readBytes(bytes);

            // 对消息内容解密
            bytes = Base64.getDecoder().decode(bytes);
            // 解码数据
            MessageMetate messageMetate = getMessageType(type);
            // 版本号不一致解析不了
            if (messageMetate == null || version != messageMetate.getVersion()) {
                return null;
            }

            // 反序列化
            Serializer serializer = getSerializer(serializerCode);
            if (messageMetate.getPacketClass() != null && serializer != null) {
                return serializer.deserialize(messageMetate.getPacketClass(), bytes);
            }
        }
        return null;
    }

    /**
     * 获取序列化算法
     *
     * @param serializerCode
     * @return
     */
    private static Serializer getSerializer(byte serializerCode) {
        return SERIALIZER_MAP.get(serializerCode);
    }

    /**
     * 获取数据包类型
     *
     * @param type
     * @return
     */
    private static MessageMetate getMessageType(byte type) {
        return MESSAGE_TYPE_PACKET_MAP.get(type);
    }

}
