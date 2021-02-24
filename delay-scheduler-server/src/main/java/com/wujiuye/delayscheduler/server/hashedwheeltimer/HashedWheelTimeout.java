package com.wujiuye.delayscheduler.server.hashedwheeltimer;

/**
 * 时间轮格子下链表的ITEM
 *
 * @author wujiuye 2021/01/13
 */
public class HashedWheelTimeout {

    /**
     * 相对时间，相对时间轮的启动时间(单位随时间轮格子的单位)
     */
    private long deadline;
    /**
     * 时间轮，第几轮
     */
    private int rounds;
    /**
     * 任务
     */
    private TimerTask task;
    /**
     * 后置节点
     */
    private HashedWheelTimeout next;
    /**
     * 前置节点
     */
    private HashedWheelTimeout prev;

    public HashedWheelTimeout(TimerTask task, long deadline) {
        this.deadline = deadline;
        this.task = task;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public TimerTask getTask() {
        return task;
    }

    public void setTask(TimerTask task) {
        this.task = task;
    }

    public HashedWheelTimeout getNext() {
        return next;
    }

    public void setNext(HashedWheelTimeout next) {
        this.next = next;
    }

    public HashedWheelTimeout getPrev() {
        return prev;
    }

    public void setPrev(HashedWheelTimeout prev) {
        this.prev = prev;
    }

}
