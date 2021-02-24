package com.github.wujiuye.raft.common;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Leader在任期内必须定期向集群内的其他节点广播心跳包，昭告自己的存在。
 *
 * @author wujiuye 2020/12/15
 */
public class HeartbeatScheduler implements Runnable {

    private long heartbeatMs;
    private HeartbeatTask heartbeatTask;
    private Thread thread;
    volatile private boolean stop;
    private AtomicLong lastMs = new AtomicLong(System.currentTimeMillis());

    @Override
    public void run() {
        while (!stop) {
            if ((System.currentTimeMillis() - lastMs.get()) >= heartbeatMs) {
                heartbeatTask.sendHeartbeat();
                lastMs.set(System.currentTimeMillis());
                continue;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public interface HeartbeatTask extends Runnable {
        @Override
        default void run() {
            sendHeartbeat();
        }

        /**
         * 发送心跳包
         */
        void sendHeartbeat();
    }

    public HeartbeatScheduler(long heartbeatMs, HeartbeatTask heartbeatTask) {
        this.heartbeatMs = heartbeatMs;
        this.heartbeatTask = heartbeatTask;
        this.thread = new Thread(this, "heartbeat-thread-" + IdUtils.newId());
        this.thread.setDaemon(true);
    }

    /**
     * 开启定时发送心跳包
     */
    public void startHeartbeat() {
        this.stop = false;
        this.heartbeatTask.sendHeartbeat();
        this.thread.start();
    }

    public void stopHeartbeat() {
        this.stop = true;
        lastMs.set(Long.MAX_VALUE);
    }

}
