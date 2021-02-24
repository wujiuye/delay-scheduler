package com.github.wujiuye.transport.netty.protocol.serialize;

import com.github.wujiuye.transport.netty.commom.JsonUtils;

import java.nio.charset.StandardCharsets;

/**
 * 使用JSON序列化反序列化
 *
 * @author wujiuye 2020/10/13
 */
public class JsonSerializer implements Serializer {

    @Override
    public byte getSerializer() {
        return SerializerEnum.JSON.getValue();
    }

    @Override
    public byte[] serialize(Object object) {
        return JsonUtils.toJsonString(object).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        return JsonUtils.fromJson(new String(bytes, StandardCharsets.UTF_8), clazz);
    }

}
