package com.github.wujiuye.raft.common;

import java.util.concurrent.locks.*;
import java.util.function.BooleanSupplier;

/**
 * @author wujiuye 2021/01/07
 */
public class CountWaiter {

    private final int count;
    private final int[] metrics;
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public CountWaiter(int count) {
        this.count = count;
        this.metrics = new int[3];
    }

    private void countDown(int index) {
        lock.lock();
        try {
            this.metrics[index]++;
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    public void countDown() {
        countDown(0);
    }

    public void countDownSuccess() {
        countDown(1);
    }

    public void countDownException() {
        countDown(2);
    }

    private void await(BooleanSupplier supplier) {
        lock.lock();
        try {
            while (!Thread.interrupted()) {
                if (supplier.getAsBoolean()) {
                    return;
                }
                condition.await();
            }
        } catch (InterruptedException ignored) {
        } finally {
            lock.unlock();
        }
    }

    public void await() {
        await(() -> (metrics[0] >= count));
    }

    public void await(int minSuccess) {
        await(() -> (metrics[0] >= count) || (metrics[1] >= minSuccess));
    }

    public int successCnt() {
        return readCnt(1);
    }

    public int exceptionCnt() {
        return readCnt(2);
    }

    private int readCnt(int index) {
        lock.lock();
        try {
            return metrics[index];
        } finally {
            lock.unlock();
        }
    }

}
