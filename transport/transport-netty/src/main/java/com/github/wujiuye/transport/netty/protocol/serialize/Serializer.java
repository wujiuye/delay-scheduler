package com.github.wujiuye.transport.netty.protocol.serialize;

/**
 * @author wujiuye 2020/10/13
 */
public interface Serializer {

    /**
     * 序列化算法
     *
     * @return
     */
    byte getSerializer();

    /**
     * java对象序列化为二进制
     */
    byte[] serialize(Object object);

    /**
     * 二进制反序列化为java对象
     */
    <T> T deserialize(Class<T> clazz, byte[] bytes);

}
