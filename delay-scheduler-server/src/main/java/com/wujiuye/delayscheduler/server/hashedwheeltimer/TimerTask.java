package com.wujiuye.delayscheduler.server.hashedwheeltimer;

/**
 * 任务
 *
 * @author wujiuye 2021/01/13
 */
public class TimerTask {

    /**
     * 任务ID（对应存储到LevelDB的key）
     */
    private String taskId;

    public TimerTask(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

}
