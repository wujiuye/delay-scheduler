package com.wujiuye.delayscheduler.client;

import com.github.wujiuye.transport.client.config.ClientConfig;
import com.wujiuye.delayscheduler.client.work.WorkManager;
import com.wujiuye.delayscheduler.core.service.ScheduleService;
import sun.misc.Signal;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author wujiuye 2021/01/20
 */
public class DelayScheduler {

    private final static AtomicReference<ClientsHolder> CLIENTS = new AtomicReference<>();
    private final static AtomicReference<ScheduleService> SERVICE = new AtomicReference<>();
    private final static AtomicReference<WorkManager> WORK_MANAGER = new AtomicReference<>();

    static {
        Signal.handle(new Signal("INT"), signal -> {
            try {
                stopClient();
            } catch (Exception ignored) {
            }
        });
    }

    private static void stopClient() throws Exception {
        SERVICE.set(null);
        if (CLIENTS.get() != null) {
            CLIENTS.get().stop();
            CLIENTS.set(null);
        }
        if (WORK_MANAGER.get() != null) {
            WORK_MANAGER.get().close();
            WORK_MANAGER.set(null);
        }
    }

    /**
     * 启动客户端，初始化调用
     *
     * @param hosts       所有节点的阈名
     * @param connTimeout 连接超时
     * @param reqTimeout  请求超时
     * @throws Exception
     */
    public synchronized static void startDelaySchedulerClient(String application, String hosts, long connTimeout, long reqTimeout) {
        if (SERVICE.get() == null) {
            String[] clientIpPorts = hosts.split(",");
            ClientConfig[] clientConfigs = new ClientConfig[clientIpPorts.length];
            for (int i = 0; i < clientConfigs.length; i++) {
                clientConfigs[i] = new ClientConfig();
                String[] ipPort = clientIpPorts[i].split(":");
                clientConfigs[i].setHost(ipPort[0]);
                clientConfigs[i].setPort(Integer.parseInt(ipPort[1]));
                clientConfigs[i].setConnectionTimeout(connTimeout);
                clientConfigs[i].setRequestTimeout(reqTimeout);
            }
            ClientsHolder clientsHolder = new ClientsHolder(clientConfigs);
            clientsHolder.start();
            ActionRpcInterfaceClient rpcInterfaceClient = new ActionRpcInterfaceClient(clientsHolder);
            SERVICE.set(new DefaultScheduleServiceImpl(application, rpcInterfaceClient));
            WORK_MANAGER.set(new WorkManager(application, rpcInterfaceClient));
            CLIENTS.set(clientsHolder);
        }
    }

    public static ScheduleService getScheduleService() {
        return SERVICE.get();
    }

}
