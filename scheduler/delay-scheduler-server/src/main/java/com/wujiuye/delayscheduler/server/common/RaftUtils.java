package com.wujiuye.delayscheduler.server.common;

import com.github.wujiuye.raft.Raft;
import com.github.wujiuye.raft.RaftNode;
import com.github.wujiuye.raft.RaftNodeRole;
import com.github.wujiuye.raft.rpc.CommandResp;
import com.github.wujiuye.transport.netty.commom.JsonUtils;

/**
 * @author wujiuye 2021/01/20
 */
public class RaftUtils {

    public static void checkMaster() {
        RaftNodeRole curNodeRole = Raft.getNode(Raft.getRaftConfig().getId()).getRaftNodeRole();
        if (curNodeRole != RaftNodeRole.Leader) {
            // 告诉客户端需要重定向到Leader节点发起请求
            for (RaftNode raftNode : Raft.getAllNodes()) {
                if (raftNode.getRaftNodeRole() == RaftNodeRole.Leader) {
                    CommandResp commandResp = new CommandResp();
                    commandResp.setSuccess(false);
                    commandResp.setRedirectToLeader(raftNode.getNodeIpPort());
                    throw new RuntimeException(JsonUtils.toJsonString(commandResp));
                }
            }
            // 还未选举出Leader
            throw new RuntimeException("the leader has not been elected");
        }
    }

}
