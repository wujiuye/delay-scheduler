package com.wujiuye.delayscheduler.server;

import com.github.wujiuye.raft.CommandLogAppender;
import com.github.wujiuye.raft.FileCommandLogAppender;
import com.github.wujiuye.raft.common.NodeIpPort;
import com.github.wujiuye.raft.common.SignalManager;
import com.github.wujiuye.raft.rpc.RaftCommandClient;
import com.github.wujiuye.raft.sdk.RaftNodeBootstarter;
import com.github.wujiuye.transport.netty.server.NettyTransportServer;
import com.github.wujiuye.transport.server.config.ServiceConfig;
import com.wujiuye.delayscheduler.server.common.LoggerUtils;
import com.wujiuye.delayscheduler.server.common.YamlReader;
import com.wujiuye.delayscheduler.server.stroage.Leveldb;
import com.wujiuye.delayscheduler.server.stroage.LeveldbKeyValueStorage;
import com.wujiuye.delayscheduler.server.stroage.KeyValueStorage;
import com.wujiuye.delayscheduler.server.config.ApplicationConfig;
import com.wujiuye.delayscheduler.server.config.ScheduleRaftConfig;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

/**
 * @author wujiuye 2021/01/12
 */
public class SchduleServerBootstarter {

    private static int loadCurNodeId(String[] args) {
        if (args == null || args.length < 2) {
            throw new NullPointerException("not config current node id.");
        }
        return Integer.parseInt(args[1]);
    }

    private static ApplicationConfig loadConfig(String[] args) {
        String activeEnv = (args == null || args.length == 0) ? "dev" : args[0];
        String envConfigFile = "application-" + activeEnv + ".yaml";
        ApplicationConfig applicationConfig = new YamlReader<ApplicationConfig>(envConfigFile) {
        }.loadConfig();
        LoggerUtils.getLogger().debug("config:{}", applicationConfig);
        return applicationConfig;
    }

    private static RaftCommandClient initRaft(int curNodeId, ScheduleRaftConfig scheduleRaftConfig,
                                              KeyValueStorage storage, CommandLogAppender commandLogAppender) {
        Set<NodeIpPort> nodeIpPorts = new HashSet<>(scheduleRaftConfig.getNodes().size());
        for (ScheduleRaftConfig.NodeConfig nodeConfig : scheduleRaftConfig.getNodes()) {
            nodeIpPorts.add(new NodeIpPort(nodeConfig.getId(), nodeConfig.getIp(), nodeConfig.getPort()));
        }
        return RaftNodeBootstarter.bootstartRaftNode(curNodeId, nodeIpPorts,
                new ActionStateMachine(storage), commandLogAppender);
    }

    private static void startDelaySchedulerServer(ServiceConfig serviceConfig, KeyValueStorage storage) {
        DelayScheduleRpcRouter delayScheduleRpcRouter = new DelayScheduleRpcRouter(new ActionRpcInterfaceImpl(storage));
        NettyTransportServer nettyTransportServer = new NettyTransportServer(delayScheduleRpcRouter);
        nettyTransportServer.start(serviceConfig);
        SignalManager.registToFirst(signal -> nettyTransportServer.stop());
    }

    private static CommandLogAppender createCommandLogAppender(int curNodeId, ApplicationConfig applicationConfig) {
        try {
            FileCommandLogAppender appender = new FileCommandLogAppender(applicationConfig.getRaft().getCommandLogPath()
                    + "/node" + curNodeId);
            close(appender);
            return appender;
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
            return null;
        }
    }

    public static void main(String[] args) {
        ApplicationConfig applicationConfig = loadConfig(args);
        ConfigConstants.setApplicationConfig(applicationConfig);
        int curNodeId = loadCurNodeId(args);

        com.wujiuye.delayscheduler.server.config.ServiceConfig serviceConfig = applicationConfig.getServer();
        List<ScheduleRaftConfig.NodeConfig> nodeConfigs = new ArrayList<>();
        int curNodePort = -1;
        for (com.wujiuye.delayscheduler.server.config.ServiceConfig.Node node : serviceConfig.getNodes()) {
            Integer nodeId = node.getNodeId();
            String[] host = node.getHost().split(":");
            nodeConfigs.add(new ScheduleRaftConfig.NodeConfig(nodeId, host[0], Integer.parseInt(host[1])));
            if (nodeId == curNodeId) {
                curNodePort = Integer.parseInt(host[2]);
            }
        }
        ScheduleRaftConfig scheduleRaftConfig = new ScheduleRaftConfig(nodeConfigs);

        Leveldb.openDb(applicationConfig.getData().getStorageRootPath() + "/node" + curNodeId);

        KeyValueStorage storage = new LeveldbKeyValueStorage();
        CommandLogAppender appender = createCommandLogAppender(curNodeId, applicationConfig);
        RaftCommandClient raftCommandClient = initRaft(curNodeId, scheduleRaftConfig, storage, appender);
        RaftConstants.holdRaftCommandClient(raftCommandClient);

        ServiceConfig nettyServiceConfig = new ServiceConfig();
        nettyServiceConfig.setIdleTimeout(applicationConfig.getServer().getIdleTimeout());
        nettyServiceConfig.setWorkThreads(applicationConfig.getServer().getWorkThreads());
        nettyServiceConfig.setPort(curNodePort);
        startDelaySchedulerServer(nettyServiceConfig, storage);

        close(raftCommandClient, storage);
    }

    private static void close(Closeable... closeables) {
        SignalManager.registToLast(signal -> {
            for (Closeable closeable : closeables) {
                try {
                    closeable.close();
                } catch (IOException ignored) {
                }
            }
            Leveldb.closeDb();
        });
    }

}
