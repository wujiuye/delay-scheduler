package com.github.wujiuye.raft.appender;

import com.github.wujiuye.raft.CommandLog;
import com.github.wujiuye.raft.common.file.AppendLogFile;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * 文件存储CommandLog
 *
 * @author wujiuye 2021/02/22
 */
public class FileCommandLogAppender implements CommandLogAppender, Closeable {

    private AppendLogFile appendLogFile;

    public FileCommandLogAppender(String rootPath) throws IOException {
        File file = new File(rootPath);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new IOException("create command log dir fail...");
            }
        }
        this.appendLogFile = new AppendLogFile(rootPath, "command-log");
    }

    @Override
    public CommandLog peek() {
        return appendLogFile.upToDateCommandLog();
    }

    @Override
    public CommandLog index(long index) {
        return appendLogFile.findCommandLog(index);
    }

    @Override
    public void append(CommandLog commandLog) {
        try {
            appendLogFile.appendLog(commandLog);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(CommandLog commandLog) {
        try {
            appendLogFile.updateLog(commandLog);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeRange(long term, long startIndex) {
        long curIndex = appendLogFile.upToDateCommandLog().getIndex();
        startIndex = startIndex < 0 ? 0 : startIndex;
        for (long ptrIndex = startIndex; ptrIndex <= curIndex; ptrIndex++) {
            try {
                appendLogFile.removeLog(ptrIndex);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public CommandLog[] range(long startIndex, long endIndex) {
        startIndex = startIndex < 0 ? 0 : startIndex;
        endIndex = endIndex < 0 ? 0 : endIndex;
        if (startIndex > endIndex) {
            return new CommandLog[0];
        }
        CommandLog[] commandLog = new CommandLog[(int) ((endIndex - startIndex) + 1)];
        int index = 0;
        for (long ptrIndex = startIndex; ptrIndex <= endIndex; ptrIndex++) {
            commandLog[index++] = appendLogFile.findCommandLog(ptrIndex);
        }
        return commandLog;
    }

    @Override
    public void close() throws IOException {
        if (appendLogFile != null) {
            appendLogFile.close();
        }
    }

}
