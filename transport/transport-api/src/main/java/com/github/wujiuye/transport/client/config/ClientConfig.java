package com.github.wujiuye.transport.client.config;

/**
 * 客户端配置
 *
 * @author wujiuye 2020/10/12
 */
public class ClientConfig {
    /**
     * IP或域名
     *
     * @return
     */
    private String host;
    /**
     * 端口
     *
     * @return
     */
    private int port;
    /**
     * 连接超时，单位毫秒
     *
     * @return
     */
    private long connectionTimeout = 5 * 1000;
    /**
     * 请求超时，单位毫秒
     *
     * @return
     */
    private long requestTimeout = 5 * 1000;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

}
