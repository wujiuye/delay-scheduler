package com.github.wujiuye.raft.common;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 选举定时器
 *
 * @author wujiuye 2020/12/15
 */
public class ElectionTimer {

    private long electionMs;
    private ElectionFunction electionFunction;
    private Thread thread;
    volatile private boolean status = false;
    /**
     * 当timer大于等于electionMs时，触发选举，调用Runnable#run方法；
     */
    private final AtomicLong lastTime = new AtomicLong(System.currentTimeMillis());

    public interface ElectionFunction {
        /**
         * 开始选举
         */
        void startElection();
    }

    public ElectionTimer(long electionMs, ElectionFunction electionFunction) {
        this.electionMs = electionMs;
        this.electionFunction = electionFunction;
        this.thread = new Thread(this::start, "election-timer-" + IdUtils.newId());
        this.thread.setDaemon(true);
    }

    public void startTimer() {
        this.thread.start();
    }

    private void start() {
        while (!status) {
            if (System.currentTimeMillis() - lastTime.get() >= electionMs) {
                callElectionFunction();
            }
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 调用选举方法
     */
    private void callElectionFunction() {
        try {
            electionFunction.startElection();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            resetTimer();
        }
    }

    /**
     * 重新计时
     * 对于Follower: 每次接收到Leader的心跳时重置
     * 对于Leader: 每次发送心跳有多数节点回应时重置
     */
    public void resetTimer() {
        lastTime.set(System.currentTimeMillis());
    }

    public void stopTimer() {
        status = true;
    }

}
