package com.github.wujiuye.raft.common;

import java.util.Objects;

/**
 * 节点的IP和端口信息
 *
 * @author wujiuye 2020/12/15
 */
public class NodeIpPort {

    /**
     * 节点ID
     */
    private int nodeId;
    /**
     * ip
     */
    private String ip;
    /**
     * 端口
     */
    private int port;

    public NodeIpPort(int nodeId, String ip, int port) {
        this.nodeId = nodeId;
        this.ip = ip;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public int getNodeId() {
        return nodeId;
    }

    public String getIp() {
        return ip;
    }

    @Override
    public String toString() {
        return "NodeIpPort{" +
                "nodeId=" + nodeId +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof NodeIpPort)) {
            return false;
        }
        NodeIpPort that = (NodeIpPort) object;
        return getNodeId() == that.getNodeId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNodeId());
    }

}
