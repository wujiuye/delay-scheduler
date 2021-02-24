package com.github.wujiuye.transport.connection;

/**
 * 抽象连接
 *
 * @author wujiuye 2020/10/12
 */
public interface Connection extends AutoCloseable {

    /**
     * 获取IP
     *
     * @return
     */
    String getRemoteIP();

    /**
     * 获取端口
     *
     * @return
     */
    int getRemotePort();

    /**
     * 远程进程ID
     */
    int getRemotePid();

    /**
     * 连接的唯一标识
     *
     * @return
     */
    String getConnectionKey();

}
