package com.github.wujiuye.raft;

import java.nio.charset.StandardCharsets;

/**
 * 日记条目
 *
 * @author wujiuye 2020/12/15
 */
public class CommandLog {

    /**
     * 提交的期号
     */
    private long term;
    /**
     * 提交时的索引
     */
    private long index;
    /**
     * 命令
     */
    private byte[] command;
    /**
     * 状态：
     * 0：未提交
     * 1：已提交
     */
    private int status = 0;

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public byte[] getCommand() {
        return command;
    }

    public void setCommand(byte[] command) {
        this.command = command;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "term=" + term +
                ", index=" + index +
                ", command=" + new String(command, StandardCharsets.UTF_8) +
                ", status=" + status +
                '}';
    }

    public String toSaveString() {
        return term + "|" + index + "|" + new String(command, StandardCharsets.UTF_8) + "|" + status;
    }

    public static CommandLog forSaveString(String str) {
        String[] info = str.split("\\|");
        CommandLog commandLog = new CommandLog();
        commandLog.setTerm(Long.parseLong(info[0]));
        commandLog.setIndex(Long.parseLong(info[1]));
        commandLog.setCommand(info[2].getBytes(StandardCharsets.UTF_8));
        commandLog.setStatus(Integer.parseInt(info[3]));
        return commandLog;
    }

}
