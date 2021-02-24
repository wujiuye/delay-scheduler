package com.github.wujiuye.transport.server.config;

/**
 * 服务端配置
 *
 * @author wujiuye 2020/10/12
 */
public class ServiceConfig {

    /**
     * 监听的端口
     *
     * @return
     */
    private int port;

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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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

}
