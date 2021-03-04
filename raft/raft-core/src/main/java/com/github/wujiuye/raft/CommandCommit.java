package com.github.wujiuye.raft;

import com.github.wujiuye.raft.appender.CommandLogAppender;

/**
 * 将日记条目提交到状态机执行
 *
 * @author wujiuye 2020/12/16
 */
public class CommandCommit {

    private CommandLogAppender commandLogAppender;
    private StateMachine stateMachine;

    public CommandCommit(CommandLogAppender commandLogAppender, StateMachine stateMachine) {
        this.commandLogAppender = commandLogAppender;
        this.stateMachine = stateMachine;
    }

    /**
     * 提交日记条目
     * 针对Raft日志条目有两个操作，提交（commit）和应用（apply），
     * 应用必须发生在提交之后，即某个日志条目只有被提交之后才能被应用到本地状态机上。
     *
     * @param commandLog 日记条目
     */
    public void commit(CommandLog commandLog) {
        if (commandLog != null) {
            // 提交（commit）
            commandLog.setStatus(1);
            commandLogAppender.update(commandLog);
            // 应用（apply）
            stateMachine.apply(commandLog);
        }
    }

    public long maxCommitIndex() {
        CommandLog commandLog = commandLogAppender.peek();
        if (commandLog == null) {
            return -1;
        }
        if (commandLog.getStatus() == 1) {
            return commandLog.getIndex();
        }
        long lastCommitIndex = commandLog.getIndex();
        while (lastCommitIndex >= 0) {
            commandLog = commandLogAppender.index(lastCommitIndex);
            if (commandLog.getStatus() == 1) {
                return commandLog.getIndex();
            }
            lastCommitIndex--;
        }
        return -1;
    }

}
