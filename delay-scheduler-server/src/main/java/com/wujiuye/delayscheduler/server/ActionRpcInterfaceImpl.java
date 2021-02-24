package com.wujiuye.delayscheduler.server;

import com.github.wujiuye.raft.common.NodeIpPort;
import com.github.wujiuye.raft.rpc.CommandResp;
import com.github.wujiuye.raft.rpc.RaftCommandClient;
import com.github.wujiuye.transport.netty.commom.JsonUtils;
import com.wujiuye.delayscheduler.core.ActionLog;
import com.wujiuye.delayscheduler.core.rpc.ActionRpcInterface;
import com.wujiuye.delayscheduler.core.ActionStat;
import com.wujiuye.delayscheduler.server.common.ActionIdGenerator;
import com.wujiuye.delayscheduler.server.common.RaftUtils;
import com.wujiuye.delayscheduler.server.config.ServiceConfig;
import com.wujiuye.delayscheduler.server.realschedule.DelaySchedulerServer;
import com.wujiuye.delayscheduler.server.stroage.KeyValueStorage;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wujiuye 2021/01/12
 */
public class ActionRpcInterfaceImpl implements ActionRpcInterface {

    private final DelaySchedulerServer schedulerServer = new DelaySchedulerServer();
    private KeyValueStorage storage;

    public ActionRpcInterfaceImpl(KeyValueStorage storage) {
        this.storage = storage;
    }

    private RaftCommandClient getRaftCommandClient() {
        return RaftConstants.getRaftCommandClient();
    }

    private RuntimeException handleNeedRedirectToLeader(CommandResp commandResp) {
        if (commandResp.getRedirectToLeader() != null) {
            List<ServiceConfig.Node> nodes = ConfigConstants.getServiceConfig().getNodes();
            ServiceConfig.Node toNode = null;
            for (ServiceConfig.Node node : nodes) {
                if (node.getNodeId().equals(commandResp.getRedirectToLeader().getNodeId())) {
                    toNode = node;
                }
            }
            if (toNode != null) {
                NodeIpPort nodeIpPort = new NodeIpPort(commandResp.getRedirectToLeader().getNodeId(),
                        commandResp.getRedirectToLeader().getIp(),
                        Integer.parseInt(toNode.getHost().split(":")[2]));
                commandResp.setRedirectToLeader(nodeIpPort);
            }
        }
        throw new RuntimeException(JsonUtils.toJsonString(commandResp));
    }

    @Override
    public Long submit(String application, ActionLog actionLog) {
        long id = ActionIdGenerator.generator();
        actionLog.setId(id);
        // submit提交 + freePeriods[nextPeriodIndex] 将是action的首次执行时间
        actionLog.setNextPeriodIndex(0);
        actionLog.setStatus(ActionStat.PENDING.getStat());
        String command = Command.SAVE.replace("{application}", application)
                .replace("{key}", String.valueOf(id))
                .replace("{value}", JsonUtils.toJsonString(actionLog));
        CommandResp commandResp = getRaftCommandClient().handleCommand(command.getBytes(StandardCharsets.UTF_8));
        if (commandResp.isSuccess()) {
            return id;
        }
        throw handleNeedRedirectToLeader(commandResp);
    }

    @Override
    public Boolean cancel(String application, Long actionId) {
        ActionLog actionLog = storage.get(application, String.valueOf(actionId));
        if (actionLog == null) {
            return false;
        }
        // 非PENDING状态不允许取消
        ActionStat curStatus = ActionStat.valueOf(actionLog.getStatus());
        if (curStatus != ActionStat.PENDING) {
            return false;
        }
        // 如果当前时间已经超过action的首次执行时间，那么不允许取消
        long firstRunTime = (actionLog.getSubmitDate() + actionLog.getFreePeriods().get(0));
        if (firstRunTime <= (System.currentTimeMillis() / 1000)) {
            return false;
        }
        // 取消
        actionLog.setStatus(ActionStat.CANCEL.getStat());
        String command = Command.SAVE.replace("{application}", application)
                .replace("{key}", String.valueOf(actionId))
                .replace("{value}", JsonUtils.toJsonString(actionLog));
        CommandResp commandResp = getRaftCommandClient().handleCommand(command.getBytes(StandardCharsets.UTF_8));
        if (commandResp.isSuccess()) {
            return true;
        }
        throw handleNeedRedirectToLeader(commandResp);
    }

    @Override
    public List<ActionLog> pull(String application, Integer maxRecord) {
        RaftUtils.checkMaster();
        synchronized (this) {
            List<Long> ids = schedulerServer.poll(application, maxRecord);
            if (ids.isEmpty()) {
                return Collections.emptyList();
            }
            return ids.parallelStream()
                    .map(id -> storage.get(application, String.valueOf(id)))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void commit(String application, Long actionId, Boolean success) {
        ActionLog actionLog = storage.get(application, String.valueOf(actionId));
        if (actionLog == null) {
            return;
        }
        if (success) {
            actionLog.setStatus(ActionStat.SUCCESS.getStat());
        } else {
            actionLog.setNextPeriodIndex(actionLog.getNextPeriodIndex() + 1);
            actionLog.setRetrySubmitDate(System.currentTimeMillis() / 1000);
            if (actionLog.getNextPeriodIndex() >= actionLog.getFreePeriods().size()) {
                actionLog.setStatus(ActionStat.FAIL.getStat());
            } else {
                // 重新就绪，等待调度
                actionLog.setStatus(ActionStat.PENDING.getStat());
            }
        }
        String command = Command.SAVE.replace("{application}", application)
                .replace("{key}", String.valueOf(actionId))
                .replace("{value}", JsonUtils.toJsonString(actionLog));
        CommandResp commandResp = getRaftCommandClient().handleCommand(command.getBytes(StandardCharsets.UTF_8));
        if (commandResp.isSuccess()) {
            return;
        }
        throw handleNeedRedirectToLeader(commandResp);
    }

}
