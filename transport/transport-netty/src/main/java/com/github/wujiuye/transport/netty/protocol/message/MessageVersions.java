package com.github.wujiuye.transport.netty.protocol.message;

/**
 * @author wujiuye 2020/10/13
 */
public interface MessageVersions {
    /**
     * 魔数
     */
    byte[] MAGIC_NUMBER = new byte[]{0xc, 0xf};
    /**
     * 版本号
     */
    byte VERSION = 0x01;
}
