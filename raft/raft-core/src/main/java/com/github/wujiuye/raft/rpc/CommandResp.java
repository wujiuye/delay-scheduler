package com.github.wujiuye.raft.rpc;

import com.github.wujiuye.raft.common.NodeIpPort;

/**
 * 响应命令请求
 *
 * @author wujiuye 2020/12/16
 */
public class CommandResp {

    /**
     * 命令是否提交成功
     */
    private boolean success;
    /**
     * 当需要重定向时
     */
    private NodeIpPort redirectToLeader;

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public NodeIpPort getRedirectToLeader() {
        return redirectToLeader;
    }

    public void setRedirectToLeader(NodeIpPort redirectToLeader) {
        this.redirectToLeader = redirectToLeader;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return "CommandResp{" +
                "success=" + success +
                ", redirectToLeader=" + redirectToLeader +
                '}';
    }

}
