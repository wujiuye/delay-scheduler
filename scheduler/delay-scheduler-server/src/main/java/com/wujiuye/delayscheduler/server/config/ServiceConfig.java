package com.wujiuye.delayscheduler.server.config;

import java.util.List;

/**
 * 服务端配置
 *
 * @author wujiuye 2020/10/12
 */
public class ServiceConfig {

    /**
     * 配置工作线程数
     *
     * @return
     */
    private int workThreads;

    /**
     * 最大空闲时间，超时关闭连接，单位秒: 要求必须比客户端的idle大，即>5秒
     *
     * @return
     */
    private int idleTimeout;

    private List<Node> nodes;

    public static class Node {
        private Integer nodeId;
        private String host;

        public void setNodeId(Integer nodeId) {
            this.nodeId = nodeId;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getNodeId() {
            return nodeId;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "nodeId=" + nodeId +
                    ", host='" + host + '\'' +
                    '}';
        }
    }

    public int getWorkThreads() {
        return workThreads;
    }

    public void setWorkThreads(int workThreads) {
        this.workThreads = workThreads;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        return "ServiceConfig{" +
                "workThreads=" + workThreads +
                ", idleTimeout=" + idleTimeout +
                ", nodes=" + nodes +
                '}';
    }

}
