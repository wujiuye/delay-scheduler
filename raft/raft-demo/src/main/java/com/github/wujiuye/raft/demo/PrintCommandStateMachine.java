package com.github.wujiuye.raft.demo;

import com.github.wujiuye.raft.CommandLog;
import com.github.wujiuye.raft.StateMachine;

/**
 * 用于测试，状态机
 *
 * @author wujiuye 2020/12/16
 */
public class PrintCommandStateMachine implements StateMachine {

    @Override
    public void apply(CommandLog execLog) {
        byte[] command = execLog.getCommand();
        String cmd = new String(command);
        System.out.println("state machine exec command: " + cmd);
    }

}
