package com.github.wujiuye.raft;

import com.github.wujiuye.raft.appender.FileCommandLogAppender;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class FileCommandLogAppenderTest {

    private FileCommandLogAppender appender;

    @Before
    public void before() throws IOException {
        File file = new File("/tmp/raft/test");
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            for (File f : files) {
                if (!f.delete()) {
                    return;
                }
            }
        }
        appender = new FileCommandLogAppender("/tmp/raft/test");
    }

    @Test
    public void test() {
        for (int i = 1; i <= 3; i++) {
            CommandLog commandLog = new CommandLog();
            commandLog.setTerm(1);
            commandLog.setIndex(i);
            commandLog.setStatus(0);
            commandLog.setCommand(("save xxx" + i).getBytes());
            appender.append(commandLog);
        }
        System.out.println("改状态");
        for (int i = 1; i <= 3; i++) {
            CommandLog log = appender.index(i);
            System.out.println(log);
            log.setStatus(2);
            appender.update(log);
        }
        System.out.println("遍历");
        for (int i = 1; i <= 3; i++) {
            CommandLog log = appender.index(i);
            System.out.println(log);
        }
        System.out.println("区间");
        CommandLog[] commandLogs = appender.range(1, 3);
        for (CommandLog log : commandLogs) {
            System.out.println(log);
        }
        System.out.println("获取头部索引");
        CommandLog commandLog = appender.peek();
        System.out.println(commandLog);
        // 移除范围记录
        appender.removeRange(1, 3);
        // 遍历
        System.out.println("移除范围后遍历");
        for (int i = 1; i <= 3; i++) {
            CommandLog log = appender.index(i);
            if (log != null) {
                System.out.println(log);
            } else {
                System.out.println("null");
            }
        }
    }

}
