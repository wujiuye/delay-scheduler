package com.github.wujiuye.transport.server.connection;

import java.util.ArrayList;
import java.util.List;

/**
 * 连接掉线监听器管理者
 *
 * @author wujiuye 2020/10/12
 */
public class ConnectionCloseListenerProvider {

    private List<ConnectionCloseListener> connectionCloseListenerList;

    private ConnectionCloseListenerProvider() {
        connectionCloseListenerList = new ArrayList<>();
    }

    private final static ConnectionCloseListenerProvider CONNECTION_CLOSE_LISTENER_PROVIDER
            = new ConnectionCloseListenerProvider();

    public static ConnectionCloseListenerProvider getInstance() {
        return CONNECTION_CLOSE_LISTENER_PROVIDER;
    }

    public synchronized void registConnectionCloseListener(ConnectionCloseListener listener) {
        this.connectionCloseListenerList.add(listener);
    }

    public synchronized void unregistAll() {
        this.connectionCloseListenerList.clear();
    }

    public synchronized List<ConnectionCloseListener> allConnectionCloseListener() {
        return connectionCloseListenerList;
    }

}
