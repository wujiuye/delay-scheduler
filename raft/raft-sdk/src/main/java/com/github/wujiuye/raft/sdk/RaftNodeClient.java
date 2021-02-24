package com.github.wujiuye.raft.sdk;

import com.github.wujiuye.raft.RaftConfig;
import com.github.wujiuye.raft.common.NodeIpPort;
import com.github.wujiuye.transport.client.config.ClientConfig;
import com.github.wujiuye.transport.netty.client.NettyTransportClient;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author wujiuye 2020/12/17
 */
class RaftNodeClient implements Closeable {

    private final int curNodeId;
    volatile private Map<Integer, NettyTransportClient> clientMap;

    public RaftNodeClient(int curNodeId) {
        this.curNodeId = curNodeId;
    }

    public synchronized void restConnectToServer(Set<NodeIpPort> nodes, RaftConfig raftConfig) {
        close();
        Map<Integer, NettyTransportClient> clientMap = new HashMap<>();
        for (NodeIpPort nodeIpPort : nodes) {
            // 不需要与自己创建连接
            if (nodeIpPort.getNodeId() == curNodeId) {
                continue;
            }
            NettyTransportClient transportClient = new NettyTransportClient();
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.setHost(nodeIpPort.getIp());
            clientConfig.setPort(nodeIpPort.getPort());
            // 请求超时控制在一次心跳超时时间内
            clientConfig.setRequestTimeout(raftConfig.getHeartbeatMs());
            clientConfig.setConnectionTimeout(raftConfig.getHeartbeatMs());
            // 掉线重新连接应控制在一次心跳超时时间内
            transportClient.start(clientConfig, raftConfig.getHeartbeatMs());
            clientMap.put(nodeIpPort.getNodeId(), transportClient);
        }
        this.clientMap = clientMap;
    }

    public NettyTransportClient getClient(int nodeId) {
        return clientMap.get(nodeId);
    }

    @Override
    public void close() {
        Map<Integer, NettyTransportClient> clientMap = this.clientMap;
        if (clientMap != null) {
            clientMap.values().forEach(client -> {
                try {
                    client.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

}
