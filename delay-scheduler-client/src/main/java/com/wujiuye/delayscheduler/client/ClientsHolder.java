package com.wujiuye.delayscheduler.client;

import com.github.wujiuye.transport.client.TransportClient;
import com.github.wujiuye.transport.client.config.ClientConfig;
import com.github.wujiuye.transport.connection.ConnectionTimeoutException;
import com.github.wujiuye.transport.connection.TransportIOException;
import com.github.wujiuye.transport.netty.client.NettyTransportClient;
import com.github.wujiuye.transport.netty.commom.JsonUtils;
import com.github.wujiuye.transport.rpc.RpcRequest;
import com.github.wujiuye.transport.rpc.RpcResponse;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author wujiuye 2021/02/05
 */
public class ClientsHolder {

    private TransportClient[] clients;
    private ClientConfig[] clientConfigs;
    private AtomicReference<TransportClient> leader = new AtomicReference<>(null);

    public ClientsHolder(ClientConfig[] clientConfigs) {
        this.clientConfigs = clientConfigs;
        this.clients = new TransportClient[clientConfigs.length];
        for (int i = 0; i < clientConfigs.length; i++) {
            this.clients[i] = new NettyTransportClient();
        }
    }

    private TransportClient getReadyClient() throws TransportIOException {
        if (leader.get() != null) {
            return leader.get();
        }
        synchronized (this) {
            int waitCntMs = 0;
            while (waitCntMs < clientConfigs[0].getConnectionTimeout()) {
                for (TransportClient client : clients) {
                    if (client.isReady()) {
                        return client;
                    }
                }
                try {
                    Thread.sleep(200);
                    waitCntMs += 200;
                } catch (Exception ignored) {
                }
            }
        }
        throw new ConnectionTimeoutException("无可用连接...");
    }

    private synchronized void resetLeader(String ip, int port) {
        TransportClient cur = leader.get();
        for (int i = 0; i < clientConfigs.length; i++) {
            ClientConfig clientConfig = clientConfigs[i];
            if (clientConfig.getHost().equalsIgnoreCase(ip) && clientConfig.getPort() == port) {
                System.out.println("redirect to leader " + ip + ":" + port);
                leader.compareAndSet(cur, clients[i]);
            }
        }
    }

    private boolean redirectByException(Throwable ex, TransportClient client) {
        try {
            Throwable cause = ex;
            while (cause != null) {
                Map<String, Object> resource = JsonUtils.fromJson(ex.getMessage(), Map.class);
                if (resource != null && resource.containsKey("redirectToLeader")) {
                    Map<String, Object> redirectToLeader = (Map<String, Object>) resource.get("redirectToLeader");
                    String ip = (String) redirectToLeader.get("ip");
                    int port = Integer.parseInt(redirectToLeader.get("port").toString());
                    resetLeader(ip, port);
                    return leader.get() != client;
                }
                cause = cause.getCause();
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    public void start() {
        for (int i = 0; i < this.clients.length; i++) {
            try {
                this.clients[i].start(clientConfigs[i]);
                waitReady(this.clients[i], 15000);
            } catch (Exception ignored) {
            }
        }
    }

    private static void waitReady(TransportClient client, long timeout) {
        long cntWaitMs = 0;
        while (!client.isReady() && cntWaitMs < timeout) {
            try {
                Thread.sleep(100);
                cntWaitMs += 100;
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void stop() {
        for (TransportClient client : this.clients) {
            try {
                client.stop();
            } catch (Exception ignored) {
            }
        }
    }

    public RpcResponse remoteInvoke(RpcRequest request) throws TransportIOException {
        TransportClient client = getReadyClient();
        try {
            RpcResponse rpcResponse = client.remoteInvoke(request);
            if (rpcResponse.getException() != null) {
                if (redirectByException(rpcResponse.getException(), client)) {
                    return leader.get().remoteInvoke(request);
                }
            }
            return rpcResponse;
        } catch (TransportIOException ex) {
            if (ex instanceof ConnectionTimeoutException) {
                leader.compareAndSet(client, null);
            }
            throw ex;
        }
    }

}
