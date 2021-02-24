package com.github.wujiuye.transport.netty.protocol.message;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 数据包协议定义
 *
 * @author wujiuye 2020/10/13
 */
public interface Message {

    /**
     * 消息类型
     *
     * @return
     */
    @JsonIgnore
    Byte getType();

    /**
     * 协议版本号
     *
     * @return
     */
    @JsonIgnore
    Byte getVersion();

}
