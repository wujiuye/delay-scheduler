package com.github.wujiuye.transport.server.connection;

import com.github.wujiuye.transport.connection.Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象连接池
 *
 * @author wujiuye 2020/10/12
 */
public abstract class AbstractConnectionPool implements ConnectionPool {

    /**
     * Format: ("ip:port", connection)
     */
    private final Map<String, Connection> CONNECTION_MAP = new ConcurrentHashMap<>();

    @Override
    public void putConnection(Connection connection) {
        CONNECTION_MAP.put(getConnectionKey(connection.getRemoteIP(), connection.getRemotePort()), connection);
    }

    private String getConnectionKey(String ip, int port) {
        return ip + ":" + port;
    }

    @Override
    public Connection getConnection(String remoteIp, int remotePort) {
        String connKey = getConnectionKey(remoteIp, remotePort);
        return CONNECTION_MAP.get(connKey);
    }

    @Override
    public void remove(String remoteIp, int remotePort) {
        Connection connection = CONNECTION_MAP.remove(getConnectionKey(remoteIp, remotePort));
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception ignored) {
            } finally {
                notifyListener(connection);
            }
        }
    }

    @Override
    public List<Connection> listAllConnection() {
        return new ArrayList<>(CONNECTION_MAP.values());
    }

    @Override
    public int count() {
        return CONNECTION_MAP.size();
    }

    @Override
    public void shutdownAll() throws Exception {
        for (Connection c : CONNECTION_MAP.values()) {
            c.close();
            notifyListener(c);
        }
    }

    /**
     * 客户端掉线通知监听器
     *
     * @param connection 客户端连接
     */
    private void notifyListener(Connection connection) {
        List<ConnectionCloseListener> closeListeners = ConnectionCloseListenerProvider.getInstance()
                .allConnectionCloseListener();
        if (closeListeners != null && !closeListeners.isEmpty()) {
            for (ConnectionCloseListener listener : closeListeners) {
                listener.onClientClose(connection);
            }
        }
    }

}
