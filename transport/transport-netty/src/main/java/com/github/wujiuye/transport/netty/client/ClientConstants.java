package com.github.wujiuye.transport.netty.client;

/**
 * 客户端状态
 *
 * @author wujiuye 2020/10/12
 */
public interface ClientConstants {

    /**
     * 关闭
     */
    int CLIENT_STATUS_OFF = 0;
    /**
     * 准备中
     */
    int CLIENT_STATUS_PENDING = 1;
    /**
     * 开启
     */
    int CLIENT_STATUS_STARTED = 2;

}
