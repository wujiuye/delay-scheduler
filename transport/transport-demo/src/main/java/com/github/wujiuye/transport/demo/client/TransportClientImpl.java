package com.github.wujiuye.transport.demo.client;

import com.github.wujiuye.transport.client.config.ClientConfig;
import com.github.wujiuye.transport.netty.client.NettyTransportClient;

import java.io.Closeable;

/**
 * @author wujiuye 2020/12/17
 */
public class TransportClientImpl implements Closeable {

    volatile private NettyTransportClient client;

    public synchronized void restConnectToServer(String ip, int port) {
        close();
        NettyTransportClient transportClient = new NettyTransportClient();
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setHost(ip);
        clientConfig.setPort(port);
        clientConfig.setRequestTimeout(1000);
        clientConfig.setConnectionTimeout(1000);
        transportClient.start(clientConfig, 5000);
        this.client = transportClient;
    }

    public NettyTransportClient getClient() {
        return client;
    }

    @Override
    public void close() {
        if (client != null) {
            try {
                client.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
