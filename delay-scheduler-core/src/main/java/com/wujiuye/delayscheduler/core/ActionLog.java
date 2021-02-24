package com.wujiuye.delayscheduler.core;

import java.io.Serializable;
import java.util.List;

/**
 * 调度日记实体
 *
 * @author wujiuye 2020/08/24
 */
public class ActionLog implements Serializable {

    private long id;
    /**
     * 任务名称
     */
    private String taskName;
    /**
     * 序列化后的参数
     */
    private String param;
    /**
     * 提交时间（单位秒）
     */
    private long submitDate;
    /**
     * 重新提交时间(单位秒)
     */
    private long retrySubmitDate;
    /**
     * 自由调度周期(单位秒)
     */
    private List<Long> freePeriods;
    /**
     * 下次重试周期的索引
     */
    private int nextPeriodIndex = 0;
    /**
     * 状态：pending、success、fail、cancel
     *
     * @see ActionStat
     */
    private String status;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public void setSubmitDate(long submitDate) {
        this.submitDate = submitDate;
    }

    public long getSubmitDate() {
        return submitDate;
    }

    public void setFreePeriods(List<Long> freePeriods) {
        this.freePeriods = freePeriods;
    }

    public List<Long> getFreePeriods() {
        return freePeriods;
    }

    public int getNextPeriodIndex() {
        return nextPeriodIndex;
    }

    public void setNextPeriodIndex(int nextPeriodIndex) {
        this.nextPeriodIndex = nextPeriodIndex;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getRetrySubmitDate() {
        return retrySubmitDate;
    }

    public void setRetrySubmitDate(long retrySubmitDate) {
        this.retrySubmitDate = retrySubmitDate;
    }

    @Override
    public String toString() {
        return "ActionLog{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", param='" + param + '\'' +
                ", submitDate=" + submitDate +
                ", retrySubmitDate=" + retrySubmitDate +
                ", freePeriods=" + freePeriods +
                ", nextPeriodIndex=" + nextPeriodIndex +
                ", status='" + status + '\'' +
                '}';
    }

}
