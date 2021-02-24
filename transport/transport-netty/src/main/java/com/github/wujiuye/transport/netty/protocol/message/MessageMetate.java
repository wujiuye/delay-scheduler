package com.github.wujiuye.transport.netty.protocol.message;

/**
 * 数据包元数据
 *
 * @author wujiuye 2020/10/13
 */
public class MessageMetate {

    /**
     * 版本号
     */
    private Byte version;
    /**
     * 数据包类型
     */
    private Class<? extends Message> packetClass;

    public MessageMetate(Byte version, Class<? extends Message> packetClass) {
        this.version = version;
        this.packetClass = packetClass;
    }

    public Byte getVersion() {
        return version;
    }

    public void setVersion(Byte version) {
        this.version = version;
    }

    public Class<? extends Message> getPacketClass() {
        return packetClass;
    }

    public void setPacketClass(Class<? extends Message> packetClass) {
        this.packetClass = packetClass;
    }

}
