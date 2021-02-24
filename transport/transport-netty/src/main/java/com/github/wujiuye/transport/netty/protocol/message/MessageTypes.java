package com.github.wujiuye.transport.netty.protocol.message;

/**
 * 消息类型常量
 *
 * @author wujiuye 2020/10/12
 */
public interface MessageTypes {

    /**
     * 心跳包类型消息
     */
    byte MSG_TYPE_PING = 0;
    /**
     * 请求类型消息-请求
     */
    byte MSG_TYPE_RPC_REQUEST = 1;
    /**
     * 请求类型消息-响应
     */
    byte MSG_TYPE_RPC_RESPONSE = 2;

}
