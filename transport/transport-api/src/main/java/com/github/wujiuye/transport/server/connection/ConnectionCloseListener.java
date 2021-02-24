package com.github.wujiuye.transport.server.connection;

import com.github.wujiuye.transport.connection.Connection;

/**
 * 连接关闭监听器
 *
 * @author wujiuye 2020/10/12
 */
public interface ConnectionCloseListener {

    /**
     * 客户端掉线
     *
     * @param connection 掉线的客户端
     */
    void onClientClose(Connection connection);

}
