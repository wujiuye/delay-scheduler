package com.github.wujiuye.transport.client.connection;

import java.util.ArrayList;
import java.util.List;

/**
 * 连接掉线监听器管理者
 *
 * @author wujiuye 2020/10/12
 */
public class ConnectionListenerProvider {

    private List<ConnectionListener> connectionCloseListenerList;

    private ConnectionListenerProvider() {
        connectionCloseListenerList = new ArrayList<>();
    }

    private final static ConnectionListenerProvider CONNECTION_CLOSE_LISTENER_PROVIDER
            = new ConnectionListenerProvider();

    public static ConnectionListenerProvider getInstance() {
        return CONNECTION_CLOSE_LISTENER_PROVIDER;
    }

    public synchronized void registConnectionFailListener(ConnectionListener listener) {
        this.connectionCloseListenerList.add(listener);
    }

    public synchronized void unregistAll() {
        this.connectionCloseListenerList.clear();
    }

    public synchronized List<ConnectionListener> allConnectionFailListener() {
        return connectionCloseListenerList;
    }

}
