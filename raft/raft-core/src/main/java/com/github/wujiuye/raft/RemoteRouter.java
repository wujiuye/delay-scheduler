package com.github.wujiuye.raft;

import com.github.wujiuye.raft.common.NodeIpPort;

/**
 * 路由器
 *
 * @author wujiuye 2020/12/16
 */
public interface RemoteRouter<T> {

    /**
     * 路由获取RPC
     *
     * @param toNode 接收节点
     * @return
     */
    T routeRpc(NodeIpPort toNode);

}
