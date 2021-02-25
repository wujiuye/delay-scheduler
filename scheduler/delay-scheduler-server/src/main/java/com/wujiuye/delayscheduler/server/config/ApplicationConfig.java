package com.wujiuye.delayscheduler.server.config;

/**
 * @author wujiuye 2021/01/12
 */
public class ApplicationConfig {

    private DataConfig data;
    private ServiceConfig server;
    private RaftConfig raft;

    public void setData(DataConfig data) {
        this.data = data;
    }

    public DataConfig getData() {
        return data;
    }

    public void setServer(ServiceConfig server) {
        this.server = server;
    }

    public ServiceConfig getServer() {
        return server;
    }

    public RaftConfig getRaft() {
        return raft;
    }

    public void setRaft(RaftConfig raft) {
        this.raft = raft;
    }

    @Override
    public String toString() {
        return "ApplicationConfig{" +
                "data=" + data +
                ", server=" + server +
                ", raft=" + raft +
                '}';
    }

}
