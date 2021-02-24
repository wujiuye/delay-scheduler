package com.wujiuye.delayscheduler.server;

/**
 * @author wujiuye 2021/01/12
 */
public interface Command {

    /**
     * 保存(含更新)
     */
    String SAVE = "save {application},{key},{value}";

    /**
     * 删除
     */
    String DEL = "del {application},{key}";

}
