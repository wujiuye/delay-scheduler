package com.github.wujiuye.transport.netty.message;

import com.github.wujiuye.transport.netty.protocol.codec.MessageCodecManager;
import com.github.wujiuye.transport.netty.protocol.message.heartbeat.Heartbeat;
import com.github.wujiuye.transport.netty.protocol.message.rpc.RpcRequestMessage;
import com.github.wujiuye.transport.netty.protocol.message.rpc.RpcResponseMessage;
import com.github.wujiuye.transport.netty.protocol.serialize.Serializer;
import com.github.wujiuye.transport.netty.protocol.serialize.SerializerEnum;
import com.github.wujiuye.transport.rpc.RpcRequest;
import com.github.wujiuye.transport.rpc.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.Test;

public class RpcMessageSerializeTest {

    @Test
    public void testRpcRequestMessage() {
        RpcRequest request = new RpcRequest();
        request.setInterfaces(Serializer.class);
        request.setMethodName("serialize");
        request.setParameterTypes(new Class<?>[]{String.class, Heartbeat.class});
        request.setArguments(new Object[]{"xxxx", new Heartbeat()});
        RpcRequestMessage message = new RpcRequestMessage(request);
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(1024);
        MessageCodecManager.encode(byteBuf, message, SerializerEnum.JSON);
        message = (RpcRequestMessage) MessageCodecManager.decode(byteBuf);
        System.out.println(message.getRpcRequest());
    }

    @Test
    public void testRpcResponseMessage() {
        RpcResponse response = new RpcResponse();
        response.setResult("sdsadasdasd");
        response.setException(new RuntimeException("xxxxxxx"));
        RpcResponseMessage rpcResponseMessage = new RpcResponseMessage(response);
        rpcResponseMessage.setTransactionId("xxxxx12312312312");
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(1024);
        MessageCodecManager.encode(byteBuf, rpcResponseMessage, SerializerEnum.JSON);
        rpcResponseMessage = (RpcResponseMessage) MessageCodecManager.decode(byteBuf);
        System.out.println(rpcResponseMessage.getRpcResponse().getResult());
        rpcResponseMessage.getRpcResponse().getException().printStackTrace();
    }

}
