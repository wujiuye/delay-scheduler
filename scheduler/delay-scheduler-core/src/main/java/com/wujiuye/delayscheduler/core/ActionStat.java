package com.wujiuye.delayscheduler.core;

/**
 * @author wjy
 */
public enum ActionStat {

    /**
     * 就绪
     */
    PENDING("pending"),
    /**
     * 调度中
     */
    DISPATCHING("dispatching"),
    /**
     * 已取消
     */
    CANCEL("cancel"),
    /**
     * 调度成功
     */
    SUCCESS("success"),
    /**
     * 调度失败
     */
    FAIL("fail");

    String stat;

    ActionStat(String stat) {
        this.stat = stat;
    }

    public String getStat() {
        return stat;
    }

    public static ActionStat valueBy(String value) {
        for (ActionStat stat : ActionStat.values()) {
            if (value.equals(stat.getStat())) {
                return stat;
            }
        }
        return null;
    }

}
