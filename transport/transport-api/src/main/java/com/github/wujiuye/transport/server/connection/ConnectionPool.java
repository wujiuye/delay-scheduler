package com.github.wujiuye.transport.server.connection;

import com.github.wujiuye.transport.connection.Connection;

import java.util.List;

/**
 * 连接池
 *
 * @author wujiuye 2020/10/12
 */
public interface ConnectionPool {

    /**
     * 缓存新的连接
     *
     * @return
     */
    void putConnection(Connection connection);

    /**
     * 获取连接
     *
     * @param remoteIp
     * @param remotePort
     * @return
     */
    Connection getConnection(String remoteIp, int remotePort);

    /**
     * 移除连接
     *
     * @param remoteIp
     * @param remotePort
     * @return
     */
    void remove(String remoteIp, int remotePort);

    /**
     * 获取所有连接
     *
     * @return
     */
    List<Connection> listAllConnection();

    /**
     * 当前连接总数
     *
     * @return
     */
    int count();

    /**
     * 关闭连接池
     *
     * @throws Exception
     */
    void shutdownAll() throws Exception;

}
