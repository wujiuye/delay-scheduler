package com.github.wujiuye.raft;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class FileCommandLogAppenderTest2 {

    private FileCommandLogAppender appender;

    @Before
    public void before() throws IOException {
        appender = new FileCommandLogAppender("/tmp/raft/test");
    }

    @Test
    public void test2() {
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
