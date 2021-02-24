package com.github.wujiuye.transport.client.connection;

import com.github.wujiuye.transport.connection.Connection;

/**
 * 连接失败监听器
 *
 * @author wujiuye 2020/10/12
 */
public interface ConnectionListener {

    /**
     * 连接服务端超时
     *
     * @param host 域名或IP
     * @param port 端口
     */
    void onConnectTimeout(String host, int port);

    /**
     * 连接关闭
     *
     * @param connection 服务端
     */
    void onConnectClose(Connection connection);

    /**
     * 连接成功
     *
     * @param connection 服务端
     */
    void onConnectSuccess(Connection connection);

}
