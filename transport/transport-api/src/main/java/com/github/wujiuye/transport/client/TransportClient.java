package com.github.wujiuye.transport.client;

import com.github.wujiuye.transport.client.config.ClientConfig;
import com.github.wujiuye.transport.connection.TransportIOException;
import com.github.wujiuye.transport.rpc.RpcRequest;
import com.github.wujiuye.transport.rpc.RpcResponse;

/**
 * 集群通信客户端
 *
 * @author wujiuye 2020/10/12
 */
public interface TransportClient {

    /**
     * 默认重连延时
     */
    int RECONNECT_DELAY_MS = 2000;

    /**
     * 获取配置
     *
     * @return
     */
    ClientConfig getConfig();

    /**
     * 启动
     *
     * @param clientConfig 客户端连接配置
     * @throws Exception
     */
    default void start(ClientConfig clientConfig) throws Exception {
        start(clientConfig, RECONNECT_DELAY_MS);
    }

    /**
     * 启动
     *
     * @param clientConfig          客户端连接配置
     * @param failRetryConnectionMs 失败重连延迟，单位Ms
     * @throws Exception
     */
    void start(ClientConfig clientConfig, long failRetryConnectionMs) throws Exception;

    /**
     * 停止
     *
     * @throws Exception
     */
    void stop() throws Exception;

    /**
     * 发起远程调用
     *
     * @param request 请求参数
     * @return 响应结果
     * @throws TransportIOException
     */
    RpcResponse remoteInvoke(RpcRequest request) throws TransportIOException;

    /**
     * 是否准备就绪
     *
     * @return
     */
    boolean isReady();

}
