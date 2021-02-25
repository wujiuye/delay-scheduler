package com.wujiuye.delayscheduler.server.hashedwheeltimer;

import com.wujiuye.delayscheduler.server.common.LoggerUtils;

import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 时间轮（只精确到秒）
 *
 * @author wujiuye 2021/01/13
 */
public class HashedWheelTimer implements Runnable, Closeable {

    private final static int INIT = 0;
    private final static int STARTED = 1;
    private final static int SHUTDOWN = 2;

    /**
     * 时间轮总的格子数
     * 60秒
     */
    private final int WHEEL_LENGTH = 60;
    private final HashedWheelBucket[] wheel;
    /**
     * 时间轮启动时间
     */
    volatile private long startTimeSecond;
    /**
     * 后台轮转线程
     */
    private Thread thread;

    private final AtomicInteger status = new AtomicInteger(INIT);
    private final CountDownLatch started = new CountDownLatch(1);

    private final LinkedBlockingQueue<HashedWheelTimeout> timeoutQueue = new LinkedBlockingQueue<>();

    private HashedWheelCallback hashedWheelCallback;

    public HashedWheelTimer(HashedWheelCallback hashedWheelCallback) {
        this.hashedWheelCallback = hashedWheelCallback;
        this.wheel = new HashedWheelBucket[WHEEL_LENGTH];
        this.thread = new Thread(this::run, "hashed_wheel_timer");
        this.thread.setDaemon(true);
    }

    public void putTask(String taskId, long submitTimeSecond, long delaySeconds) {
        start();
        TimerTask timerTask = new TimerTask(taskId);
        delaySeconds = (submitTimeSecond + delaySeconds) - startTimeSecond;
        if (delaySeconds > 0) {
            HashedWheelTimeout hashedWheelTimeout = new HashedWheelTimeout(timerTask, delaySeconds);
            timeoutQueue.add(hashedWheelTimeout);
            return;
        }
        // 立即执行
        hashedWheelCallback.runWork(-1, Collections.singletonList(timerTask));
    }

    private void start() {
        switch (status.get()) {
            case INIT:
                if (status.compareAndSet(INIT, STARTED)) {
                    thread.start();
                }
                break;
            case STARTED:
                break;
            case SHUTDOWN:
                throw new IllegalStateException("cannot be started once stopped");
            default:
                throw new Error("Invalid State");
        }
        while (startTimeSecond == 0) {
            try {
                started.await();
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Override
    public void close() {
        LoggerUtils.getLogger().debug("hashed wheel timer close...");
        status.set(SHUTDOWN);
    }

    @Override
    public void run() {
        this.startTimeSecond = System.currentTimeMillis() / 1000;
        started.countDown();
        while (status.get() != SHUTDOWN) {
            processHashedWheelTimeoutQueue();
            runWork();
        }
    }

    private void processHashedWheelTimeoutQueue() {
        int cnt = 0;
        // 避免数据过多，占用完当前期的时间
        while (cnt < 100000) {
            try {
                HashedWheelTimeout hashedWheelTimeout = timeoutQueue.poll(10, TimeUnit.MILLISECONDS);
                if (hashedWheelTimeout == null) {
                    return;
                }
                cnt++;
                long deadline = hashedWheelTimeout.getDeadline();
                int index = (int) (deadline % WHEEL_LENGTH);
                hashedWheelTimeout.setRounds((int) deadline);
                if (wheel[index] == null) {
                    wheel[index] = new HashedWheelBucket();
                }
                wheel[index].addNode(hashedWheelTimeout);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void runWork() {
        int curDeadline = (int) (((System.currentTimeMillis() / 1000) - startTimeSecond));
        int curRounds = curDeadline;
        int index = curDeadline % WHEEL_LENGTH;
        if (wheel[index] == null) {
            wheel[index] = new HashedWheelBucket();
        }
        List<TimerTask> timeouts = wheel[index].removeTimeout(curRounds);
        if (timeouts.isEmpty()) {
            return;
        }
        hashedWheelCallback.runWork(curRounds, timeouts);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("HashedWheelTimer[");
        for (HashedWheelBucket bucket : wheel) {
            if (bucket != null) {
                builder.append(bucket.toString()).append("\n");
            } else {
                builder.append("null\n");
            }
        }
        builder.append("]\n\n");
        return builder.toString();
    }

}
