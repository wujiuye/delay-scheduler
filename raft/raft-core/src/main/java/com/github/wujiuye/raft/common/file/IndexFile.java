package com.github.wujiuye.raft.common.file;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 索引文件
 *
 * @author wujiuye 2021/02/22
 */
public class IndexFile implements Closeable {

    private final static String SUFFIX = ".idx";
    private final static int MAX_RECORD = 4096;

    private String rootPath, logFileName;

    private AtomicReference<String> curFile = new AtomicReference<>(null);
    private AtomicReference<FileOutputStream> outputStream = new AtomicReference<>(null);
    private AtomicLong curIndex;
    private AtomicLong curIndexFileStart;

    public IndexFile(String rootPath, String logFileName) throws IOException {
        this.rootPath = rootPath;
        this.logFileName = logFileName;
        ensureFileExist();
        curFile.set(listSortFiles().get(0));
        curIndex = new AtomicLong(initReadCurIndexFileRecords());
    }

    private void removeExpireFile(int before) throws IOException {
        List<String> files = listSortFiles();
        if (files.size() > before) {
            for (int i = before; i <= files.size(); i++) {
                File file = new File(rootPath + "/" + files.get(i));
                file.delete();
            }
        }
    }

    private void ensureFileExist() throws IOException {
        List<String> files = listSortFiles();
        if (files.isEmpty()) {
            createNewFile(0);
        } else {
            String fileName = files.get(0);
            curFile.set(fileName);
            // 不将append设置为true会导致文件内容被清空
            outputStream.set(new FileOutputStream(rootPath + "/" + fileName, true));
        }
        removeExpireFile(7);
    }

    private void createNewFile(long newFileStartIndex) throws IOException {
        String newFileName = logFileName + ".";
        newFileName += (newFileStartIndex + "-" + (newFileStartIndex + MAX_RECORD - 1));
        newFileName += SUFFIX;
        File file = new File(rootPath + "/" + newFileName);
        if (!file.exists()) {
            if (file.createNewFile()) {
                if (outputStream.get() != null) {
                    outputStream.get().close();
                }
                curFile.set(newFileName);
                outputStream.set(new FileOutputStream(file, true));
            }
        } else if (outputStream.get() == null) {
            curFile.set(newFileName);
            outputStream.set(new FileOutputStream(file, true));
        }
    }

    private List<String> listSortFiles() {
        File file = new File(rootPath);
        if (!file.exists()) {
            file.mkdirs();
            return Collections.emptyList();
        }
        if (!file.isDirectory()) {
            file.delete();
            file.mkdirs();
            return Collections.emptyList();
        }
        String[] fileNames = file.list((dir, name) -> name.startsWith(logFileName + ".") && name.endsWith(SUFFIX));
        if (fileNames == null || fileNames.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.stream(fileNames).sorted((fn1, fn2) -> {
            String[] fn1info = fn1.substring(logFileName.length()).split("\\.");
            String[] indexRange = fn1info[1].split("-");
            long indexStart1 = Long.parseLong(indexRange[0]);
            String[] fn2info = fn2.substring(logFileName.length()).split("\\.");
            String[] indexRange2 = fn2info[1].split("-");
            long indexStart2 = Long.parseLong(indexRange2[0]);
            return (int) (indexStart1 - indexStart2);
        }).collect(Collectors.toList());
    }

    private long initReadCurIndexFileRecords() {
        List<String> files = listSortFiles();
        String fileName = files.get(0);
        String endStr = fileName.substring(logFileName.length() + 1);
        String[] fninfo = endStr.split("\\.");
        String[] indexRange = fninfo[0].split("-");
        long indexStart = Long.parseLong(indexRange[0]);
        curIndexFileStart = new AtomicLong(indexStart);
        File file = new File(rootPath + "/" + fileName);
        return indexStart + file.length() / indexLength() - 1; // index start by 0
    }

    public synchronized void appendOffset(long index, long offset) throws IOException {
        if (index < curIndex.get()) {
            throw new RuntimeException("index err.");
        }
        for (; index > curIndex.get(); ) {
            if (curIndex.get() - curIndexFileStart.get() >= MAX_RECORD) {
                createNewFile(curIndexFileStart.get() + MAX_RECORD);
            }
            FileOutputStream fos = outputStream.get();
            fos.getChannel().position((curIndex.get() + 1 - curIndexFileStart.get()) * indexLength());
            curIndex.incrementAndGet();
            fos.getChannel().write(ByteBuffer.wrap(toByte(curIndex.get())));
            fos.getChannel().write(ByteBuffer.wrap(toByte(index == curIndex.get() ? offset : Long.MIN_VALUE)));
        }
    }

    public synchronized void updateOffset(long index, long offset) throws IOException {
        Postion postion = postionByIndex(index);
        if (postion == null) {
            throw new NullPointerException("not found record by index " + index);
        }
        // 会导致文件此位置之后的内容被清空
        // try (FileOutputStream fileInputStream = new FileOutputStream(rootPath + "/" + postion.fileName, false)) {
        //    fileInputStream.getChannel().position((index - postion.startIndex) * (8 + 8));
        //    fileInputStream.getChannel().write(ByteBuffer.wrap(toByte(index)));
        //    fileInputStream.getChannel().write(ByteBuffer.wrap(toByte(offset)));
        // }
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(rootPath + "/" + postion.fileName, "rw")) {
            randomAccessFile.seek((index - postion.startIndex) * indexLength());
            randomAccessFile.write(toByte(index));
            randomAccessFile.write(toByte(offset));
        }
    }

    private static byte[] toByte(long value) {
        byte[] writeBuffer = new byte[8];
        writeBuffer[0] = (byte) (value >>> 56);
        writeBuffer[1] = (byte) (value >>> 48);
        writeBuffer[2] = (byte) (value >>> 40);
        writeBuffer[3] = (byte) (value >>> 32);
        writeBuffer[4] = (byte) (value >>> 24);
        writeBuffer[5] = (byte) (value >>> 16);
        writeBuffer[6] = (byte) (value >>> 8);
        writeBuffer[7] = (byte) (value);
        return writeBuffer;
    }

    private static int indexLength() {
        return toByte(0).length * 2;
    }

    private Postion postionByIndex(long index) {
        Postion postion = null;
        if (curIndexFileStart.get() >= index) {
            postion = new Postion();
            postion.setFileName(curFile.get());
            postion.setStartIndex(curIndexFileStart.get());
            postion.setEndIndex(curIndexFileStart.get() + MAX_RECORD - 1);
        } else {
            List<String> files = listSortFiles();
            for (String fn : files) {
                String[] fninfo = fn.substring(logFileName.length()).split("\\.");
                String[] indexRange = fninfo[1].split("-");
                long indexStart = Long.parseLong(indexRange[0]);
                long indexEnd = Long.parseLong(indexRange[1]);
                if (index >= indexStart && index <= indexEnd) {
                    postion = new Postion();
                    postion.setFileName(fn);
                    postion.setStartIndex(indexStart);
                    postion.setEndIndex(indexEnd);
                    break;
                }
            }
        }
        return postion;
    }

    static class Postion {
        private String fileName;
        private long startIndex;
        private long endIndex;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public long getStartIndex() {
            return startIndex;
        }

        public void setStartIndex(long startIndex) {
            this.startIndex = startIndex;
        }

        public long getEndIndex() {
            return endIndex;
        }

        public void setEndIndex(long endIndex) {
            this.endIndex = endIndex;
        }
    }

    static class Offset {
        private String logFileName;
        private long physicsOffset;

        public Offset(String logFileName, long physicsOffset) {
            this.logFileName = logFileName;
            this.physicsOffset = physicsOffset;
        }

        public long getPhysicsOffset() {
            return physicsOffset;
        }

        public String getLogFileName() {
            return logFileName;
        }
    }

    public Offset findOffset(long index) {
        Postion postion = postionByIndex(index);
        long offset = -1;
        if (postion != null) {
            try (FileInputStream fileInputStream = new FileInputStream(rootPath + "/" + postion.fileName)) {
                fileInputStream.getChannel().position((index - postion.startIndex) * indexLength());
                ByteBuffer buffer = ByteBuffer.allocate(8);
                for (; ; ) {
                    if (fileInputStream.getChannel().read(buffer) != 8) {
                        break;
                    }
                    buffer.flip();
                    long indexRead = buffer.getLong();
                    buffer.clear();
                    if (fileInputStream.getChannel().read(buffer) != 8) {
                        break;
                    }
                    buffer.flip();
                    offset = buffer.getLong();
                    buffer.clear();
                    if (indexRead == index) {
                        break;
                    }
                }
                buffer.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (offset >= 0) {
                return new Offset(postion.fileName.split("\\.")[0], offset);
            } else if (offset == Long.MIN_VALUE) {
                // 索引被标志为删除Long.MIN_VALUE
                return null;
            }
        }
        // 不存在索引
        return null;
    }

    /**
     * 获取最新的一个索引（有效的，未被标志为删除的）
     *
     * @return 物理偏移
     */
    public Offset latestEffectiveIndexOffset() {
        if (curIndex.get() < 0) {
            return null;
        }
        long ptr = curIndex.get();
        do {
            Offset offset = findOffset(curIndex.get());
            if (offset != null) {
                return offset;
            }
        } while ((ptr--) >= curIndexFileStart.get());
        return null;
    }

    public void clear() throws IOException {
        removeExpireFile(0);
    }

    @Override
    public void close() throws IOException {
        if (outputStream != null) {
            outputStream.get().close();
        }
    }

}
