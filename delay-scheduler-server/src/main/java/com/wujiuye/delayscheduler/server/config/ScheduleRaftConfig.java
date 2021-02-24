package com.wujiuye.delayscheduler.server.config;

import java.util.List;

/**
 * @author wujiuye 2021/01/12
 */
public class ScheduleRaftConfig {

    private List<NodeConfig> nodes;

    public ScheduleRaftConfig(List<NodeConfig> nodes) {
        this.nodes = nodes;
    }

    public List<NodeConfig> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeConfig> nodes) {
        this.nodes = nodes;
    }

    public static class NodeConfig {
        private Integer id;
        private String ip;
        private Integer port;

        public NodeConfig(Integer id, String ip, Integer port) {
            this.id = id;
            this.ip = ip;
            this.port = port;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        @Override
        public String toString() {
            return "NodeConfig{" +
                    "id=" + id +
                    ", ip='" + ip + '\'' +
                    ", port=" + port +
                    '}';
        }

    }

    @Override
    public String toString() {
        return "ScheduleRaftConfig{" +
                "nodes=" + nodes +
                '}';
    }

}
