package com.wujiuye.delayscheduler.server.config;

/**
 * @author wujiuye 2021/01/12
 */
public class RaftConfig {

    private String commandLogPath;

    public void setCommandLogPath(String commandLogPath) {
        this.commandLogPath = commandLogPath;
    }

    public String getCommandLogPath() {
        return commandLogPath;
    }

}
