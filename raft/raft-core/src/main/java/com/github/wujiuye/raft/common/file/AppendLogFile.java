package com.github.wujiuye.raft.common.file;

import com.github.wujiuye.raft.CommandLog;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 日记文件
 *
 * @author wujiuye 2021/02/22
 */
public class AppendLogFile implements Closeable {

    private final static String SUFFIX = ".log";

    private String rootPath, logFileName;

    private AtomicReference<IndexFileV3> indexFile = new AtomicReference<>(null);
    private AtomicReference<String> curFile = new AtomicReference<>(null);
    private AtomicLong curFileLen = new AtomicLong(0);
    private AtomicReference<FileOutputStream> outputStream = new AtomicReference<>(null);

    public AppendLogFile(String rootPath, String logFileName) throws IOException {
        this.rootPath = rootPath;
        this.logFileName = logFileName;
        ensureFileExist();
    }

    public synchronized void appendLog(CommandLog log) throws IOException {
        appendLog(log, true);
    }

    private long appendLog(CommandLog log, boolean appendIndex) throws IOException {
        ensureFileExist();
        // 追加日记
        long offset = curFileLen.get();
        byte[] bytes = (log.toSaveString() + "\n").getBytes(StandardCharsets.UTF_8);
        outputStream.get().write(bytes);
        // 追加索引
        if (appendIndex) {
            indexFile.get().appendOffset(log.getIndex(), offset);
        }
        return offset;
    }

    public synchronized void removeLog(long index) throws IOException {
        // 只移除索引
        indexFile.get().updateOffset(index, Long.MIN_VALUE);
    }

    public synchronized void updateLog(CommandLog log) throws IOException {
        // 在原文件中追加一行
        long newOffset = appendLog(log, false);
        // 更新索引
        indexFile.get().updateOffset(log.getIndex(), newOffset);
    }

    public CommandLog findCommandLog(long index) {
        if (index < 0) {
            return null;
        }
        IndexFileV3.Offset offset = indexFile.get().findOffset(index);
        if (offset == null) {
            return null;
        }
        return findCommandLog(offset);
    }

    private CommandLog findCommandLog(IndexFileV3.Offset offset) {
        try (FileInputStream fileInputStream = new FileInputStream(rootPath + "/" + offset.getLogFileName() + SUFFIX)) {
            fileInputStream.getChannel().position(offset.getPhysicsOffset());
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            int len;
            byte[] bytes = new byte[8];
            loop:
            while ((len = fileInputStream.read(bytes)) > 0) {
                for (int i = 0; i < len; i++) {
                    if (bytes[i] == '\n') {
                        buffer.put(bytes, 0, i);
                        break loop;
                    }
                }
                buffer.put(bytes, 0, len);
            }
            if (buffer.position() == 0) {
                return null;
            }
            buffer.flip();
            String log = new String(buffer.array(), buffer.position(), buffer.limit(), StandardCharsets.UTF_8);
            return CommandLog.forSaveString(log);
        } catch (IOException e) {
            return null;
        }
    }

    public CommandLog upToDateCommandLog() {
        IndexFileV3.Offset offset = indexFile.get().latestEffectiveIndexOffset();
        if (offset == null) {
            return null;
        }
        return findCommandLog(offset);
    }

    public void clear() throws IOException {
        removeExpireFile(0);
    }

    @Override
    public void close() throws IOException {
        if (outputStream.get() != null) {
            outputStream.get().close();
        }
        if (indexFile.get() != null) {
            indexFile.get().close();
        }
    }

    private void removeExpireFile(int before) throws IOException {
        List<String> files = listFiles();
        if (files.size() > before) {
            for (int i = before; i <= files.size(); i++) {
                File file = new File(rootPath + "/" + files.get(i));
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    private void ensureFileExist() throws IOException {
        createNewFile();
        removeExpireFile(3);
    }

    private void createNewFile() throws IOException {
        String newFileName = logFileName + "-";
        newFileName += LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String newIndexFileName = newFileName;
        newFileName += SUFFIX;
        File file = new File(rootPath + "/" + newFileName);
        if (!file.exists()) {
            if (file.createNewFile()) {
                if (outputStream.get() != null) {
                    outputStream.get().close();
                }
                if (indexFile.get() != null) {
                    indexFile.get().close();
                }
                curFile.set(newFileName);
                outputStream.set(new FileOutputStream(file, true));
                indexFile.set(new IndexFileV3(rootPath, newIndexFileName));
            }
        } else if (outputStream.get() == null) {
            curFile.set(newFileName);
            outputStream.set(new FileOutputStream(file, true));
            indexFile.set(new IndexFileV3(rootPath, newIndexFileName));
        }
        curFileLen.set(file.length());
    }

    private List<String> listFiles() throws IOException {
        File file = new File(rootPath);
        if (!file.exists()) {
            file.createNewFile();
            return Collections.emptyList();
        }
        if (!file.isDirectory()) {
            file.delete();
            file.createNewFile();
            return Collections.emptyList();
        }
        String[] fileNames = file.list((dir, name) -> name.startsWith(logFileName + "-") && name.endsWith(SUFFIX));
        if (fileNames == null || fileNames.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.stream(fileNames).collect(Collectors.toList());
    }

}
