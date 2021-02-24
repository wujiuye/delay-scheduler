package com.github.wujiuye.transport.netty.protocol.serialize;

/**
 * 序列化算法枚举类型
 *
 * @author wujiuye 2020/10/13
 */
public enum SerializerEnum {

    /**
     * json序列化算法
     */
    JSON((byte) 0x01),
    JDK((byte) 0x02);

    /**
     * 算法只能占用数据包的一个字节
     */
    byte value;

    SerializerEnum(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return this.value;
    }

}
