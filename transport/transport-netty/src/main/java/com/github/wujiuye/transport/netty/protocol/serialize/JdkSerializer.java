package com.github.wujiuye.transport.netty.protocol.serialize;

import java.io.*;

/**
 * @author wujiuye 2021/01/25
 */
public class JdkSerializer implements Serializer {

    @Override
    public byte getSerializer() {
        return SerializerEnum.JDK.value;
    }

    @Override
    public byte[] serialize(Object object) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(object);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return (T) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
