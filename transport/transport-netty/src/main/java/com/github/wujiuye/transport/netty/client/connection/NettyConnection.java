package com.github.wujiuye.transport.netty.client.connection;

import com.github.wujiuye.transport.client.connection.ConnectionListener;
import com.github.wujiuye.transport.client.connection.ConnectionListenerProvider;
import com.github.wujiuye.transport.connection.Connection;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Netty连接
 *
 * @author wujiuye 2020/10/12
 */
public class NettyConnection implements Connection {

    private Channel channel;

    public NettyConnection(Channel channel) {
        this.channel = channel;
        List<ConnectionListener> connectionListenerLsit = ConnectionListenerProvider.getInstance().allConnectionFailListener();
        if (connectionListenerLsit != null && !connectionListenerLsit.isEmpty()) {
            for (ConnectionListener listener : connectionListenerLsit) {
                listener.onConnectSuccess(this);
            }
        }
    }

    @Override
    public String getRemoteIP() {
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        return socketAddress.getAddress().getHostAddress();
    }

    @Override
    public int getRemotePort() {
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        return socketAddress.getPort();
    }

    @Override
    public int getRemotePid() {
        Object pid = this.channel.attr(AttributeKey.valueOf("PID")).get();
        return pid == null ? -1 : (int) pid;
    }

    @Override
    public String getConnectionKey() {
        return getRemoteIP() + ":" + getRemotePort();
    }

    @Override
    public void close() throws Exception {
        List<ConnectionListener> connectionListenerLsit = ConnectionListenerProvider.getInstance().allConnectionFailListener();
        if (connectionListenerLsit != null && !connectionListenerLsit.isEmpty()) {
            for (ConnectionListener listener : connectionListenerLsit) {
                listener.onConnectClose(this);
            }
        }
        channel = null;
    }

    public Channel getChannel() {
        return channel;
    }

}
