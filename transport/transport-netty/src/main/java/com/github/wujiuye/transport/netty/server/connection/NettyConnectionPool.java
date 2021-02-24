package com.github.wujiuye.transport.netty.server.connection;

import com.github.wujiuye.transport.connection.Connection;
import com.github.wujiuye.transport.server.connection.AbstractConnectionPool;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

/**
 * 连接池管理连接
 *
 * @author wujiuye 2020/10/12
 */
public class NettyConnectionPool extends AbstractConnectionPool {

    public void createConnection(Channel channel) {
        if (channel != null) {
            Connection connection = new NettyConnection(channel);
            putConnection(connection);
        }
    }

    public void remove(Channel channel) {
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        String remoteIp = socketAddress.getAddress().getHostAddress();
        int remotePort = socketAddress.getPort();
        remove(remoteIp, remotePort);
    }

}

