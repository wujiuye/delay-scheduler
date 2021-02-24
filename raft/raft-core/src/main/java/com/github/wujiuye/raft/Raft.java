package com.github.wujiuye.raft;

import com.github.wujiuye.raft.common.HeartbeatCallable;
import com.github.wujiuye.raft.common.NodeIpPort;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 入口
 *
 * @author wujiuye 2020/12/15
 */
public class Raft {

    private static final RaftConfig RAFT_CONFIG = new RaftConfig();

    /**
     * Raft所有节点
     *
     * @key 节点ID
     * @value 节点信息
     */
    private static Map<Integer, RaftNode> NODES = new HashMap<>();

    private final static AtomicReference<HeartbeatCallable> HEARTBEAT_CALLABLE_REFERENCE = new AtomicReference<>(null);

    /**
     * 初始化
     *
     * @param nodes 集群中的所有节点
     */
    public static synchronized void init(Set<NodeIpPort> nodes) {
        setAllNode(nodes);
    }

    /**
     * 写入集群节点
     *
     * @param nodes 集群中的所有节点
     */
    private static void setAllNode(Set<NodeIpPort> nodes) {
        for (NodeIpPort nodeIpPort : nodes) {
            RaftNode node = new RaftNode(nodeIpPort.getNodeId());
            node.setNodeIpPort(nodeIpPort);
            NODES.put(node.getId(), node);
        }
    }

    /**
     * 获取集群中的所有节点
     *
     * @return
     */
    public static Set<RaftNode> getAllNodes() {
        return new HashSet<>(NODES.values());
    }

    /**
     * 根据节点ID获取节点信息
     *
     * @param nodeId 节点ID
     * @return
     */
    public static RaftNode getNode(Integer nodeId) {
        return NODES.get(nodeId);
    }

    /**
     * 获取Raft节点总数
     *
     * @return
     */
    public static int nodeCount() {
        return NODES.size();
    }

    public static RaftConfig getRaftConfig() {
        return RAFT_CONFIG;
    }

    public static HeartbeatCallable getHeartbeatCallback() {
        return HEARTBEAT_CALLABLE_REFERENCE.get();
    }

    public static synchronized void holdHeartbeatCallable(HeartbeatCallable heartbeatCallable) {
        HEARTBEAT_CALLABLE_REFERENCE.set(heartbeatCallable);
    }

}
