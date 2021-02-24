package com.github.wujiuye.transport.netty.server.connection;

import com.github.wujiuye.transport.connection.Connection;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;

/**
 * @author wujiuye 2020/10/12
 */
public class NettyConnection implements Connection {

    private String remoteIp;
    private int remotePort;
    private int remotePid;
    private Channel channel;

    public NettyConnection(Channel channel) {
        this.channel = channel;
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        this.remoteIp = socketAddress.getAddress().getHostAddress();
        this.remotePort = socketAddress.getPort();
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public int getRemotePort() {
        return remotePort;
    }

    @Override
    public String getRemoteIP() {
        return remoteIp;
    }

    @Override
    public int getRemotePid() {
        if (remotePid == 0) {
            Object pid = this.channel.attr(AttributeKey.valueOf("PID")).get();
            remotePid = (pid == null ? -1 : (int) pid);
        }
        return remotePid;
    }

    @Override
    public String getConnectionKey() {
        return remoteIp + ":" + remotePort;
    }

    @Override
    public void close() {
        if (channel != null && channel.isActive()) {
            channel.close();
        }
    }

}