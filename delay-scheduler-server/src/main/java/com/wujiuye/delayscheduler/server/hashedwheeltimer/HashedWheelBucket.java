package com.wujiuye.delayscheduler.server.hashedwheeltimer;

import java.util.ArrayList;
import java.util.List;

/**
 * 时间轮格子
 *
 * @author wujiuye 2021/01/13
 */
public class HashedWheelBucket {

    /**
     * 链表的头节点
     */
    private HashedWheelTimeout head;
    /**
     * 链表的尾节点
     */
    private HashedWheelTimeout tail;

    /**
     * 添加的同时移除过期的
     *
     * @param timeout
     */
    public void addNode(HashedWheelTimeout timeout) {
        if (head == null) {
            head = timeout;
            tail = head;
            return;
        }
        HashedWheelTimeout ptr = head;
        for (; ptr != null && timeout.getRounds() > ptr.getRounds(); ptr = ptr.getNext()) {
        }
        if (ptr != null) {
            timeout.setNext(ptr.getNext());
            timeout.setPrev(ptr);
            ptr.setNext(timeout);
        } else {
            timeout.setPrev(tail);
            tail.setNext(timeout);
            tail = timeout;
        }
    }

    /**
     * 取当前轮需要执行的
     *
     * @param curRounds
     * @return
     */
    public List<TimerTask> removeTimeout(long curRounds) {
        List<TimerTask> list = new ArrayList<>();
        HashedWheelTimeout ptr = head;
        for (; ptr != null && ptr.getRounds() <= curRounds; ptr = ptr.getNext()) {
            if (ptr.getRounds() <= curRounds) {
                list.add(ptr.getTask());
                continue;
            }
            break;
        }
        if (ptr == null) {
            head = null;
            tail = null;
        } else {
            head = ptr;
        }
        return list;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("HashedWheelBucket[");
        HashedWheelTimeout ptr = head;
        for (; ptr != null; ptr = ptr.getNext()) {
            builder.append("{")
                    .append("rounds=").append(ptr.getRounds()).append(",")
                    .append("taskId=").append(ptr.getTask().getTaskId())
                    .append("}").append(",");
        }
        builder.append("]");
        return builder.toString();
    }

}
