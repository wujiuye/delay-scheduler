package com.github.wujiuye.raft.common.file;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private final static int MAX_RECORD = 65535;

    private String rootPath, logFileName;

    private AtomicReference<String> curFile = new AtomicReference<>(null);
    private AtomicReference<MappedByteBuffer> mappedByteBuffer = new AtomicReference<>(null);
    private AtomicLong curIndexFileStart = new AtomicLong(-1), curIndexFileEnd = new AtomicLong(-1);
    private Thread thread;
    private AtomicBoolean force = new AtomicBoolean(true);

    public IndexFile(String rootPath, String logFileName) throws IOException {
        this.rootPath = rootPath;
        this.logFileName = logFileName;
        ensureFileExist();
        curFile.set(listSortFiles().get(0));
        thread = new Thread(() -> {
            while (force.get()) {
                if (mappedByteBuffer.get() != null) {
                    mappedByteBuffer.get().force();
                }
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException ignored) {
                }
            }
        }, "index-force");
        thread.start();
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
            String endStr = fileName.substring(logFileName.length() + 1);
            String[] fninfo = endStr.split("\\.");
            String[] indexRange = fninfo[0].split("-");
            long indexStart = Long.parseLong(indexRange[0]);
            long indexEnd = Long.parseLong(indexRange[1]);
            curIndexFileStart.set(indexStart);
            curIndexFileEnd.set(indexEnd);
            curFile.set(fileName);
            FileChannel fileChannel = FileChannel.open(Paths.get(URI.create("file:" + rootPath + "/" + fileName)), StandardOpenOption.WRITE, StandardOpenOption.READ);
            mappedByteBuffer.set(fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, headerLength() + MAX_RECORD * indexLength()));
            fileChannel.close();
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
                if (mappedByteBuffer.get() != null) {
                    mappedByteBuffer.get().force();
                }
                curFile.set(newFileName);
                FileChannel fileChannel = FileChannel.open(Paths.get(URI.create("file:" + rootPath + "/" + newFileName)), StandardOpenOption.WRITE, StandardOpenOption.READ);
                mappedByteBuffer.set(fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, headerLength() + MAX_RECORD * indexLength()));
                fileChannel.close();
                curIndexFileStart.set(newFileStartIndex);
                curIndexFileEnd.set((newFileStartIndex + MAX_RECORD - 1));
            }
        } else if (mappedByteBuffer.get() == null) {
            curFile.set(newFileName);
            FileChannel fileChannel = FileChannel.open(Paths.get(URI.create("file:" + rootPath + "/" + newFileName)), StandardOpenOption.WRITE, StandardOpenOption.READ);
            mappedByteBuffer.set(fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, headerLength() + MAX_RECORD * indexLength()));
            fileChannel.close();
            curIndexFileStart.set(newFileStartIndex);
            curIndexFileEnd.set((newFileStartIndex + MAX_RECORD - 1));
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

    public synchronized void appendOffset(long index, long offset) throws IOException {
        if (index < curIndexFileStart.get()) {
            updateOffset(index, offset);
            return;
        }
        if (index > curIndexFileEnd.get()) {
            createNewFile(curIndexFileEnd.get() + 1);
        }
        mappedByteBuffer.get().position((int) ((index - curIndexFileStart.get()) * indexLength() + headerLength()));
        mappedByteBuffer.get().put(ByteBuffer.wrap(toByte(index)));
        mappedByteBuffer.get().put(ByteBuffer.wrap(toByte(offset)));
        writeFileHeader(mappedByteBuffer.get(), index);
    }

    public synchronized void updateOffset(long index, long offset) throws IOException {
        if (index >= curIndexFileStart.get() && index <= curIndexFileEnd.get()) {
            mappedByteBuffer.get().position((int) ((index - curIndexFileStart.get()) * indexLength() + headerLength()));
            mappedByteBuffer.get().put(ByteBuffer.wrap(toByte(index)));
            mappedByteBuffer.get().put(ByteBuffer.wrap(toByte(offset)));
            return;
        }
        Postion postion = postionByIndex(index);
        if (postion == null) {
            throw new NullPointerException("not found record by index " + index);
        }
        // 非频率写，不用MappedByteBuffer
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(rootPath + "/" + postion.fileName, "rw")) {
            randomAccessFile.seek((index - postion.startIndex) * indexLength() + headerLength());
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

    private static long toLong(byte[] bytes, int offset) {
        long value = ((long) (bytes[offset]) & 0xff) << 56;
        value |= ((long) (bytes[offset + 1]) & 0xff) << 48;
        value |= ((long) (bytes[offset + 2]) & 0xff) << 40;
        value |= ((long) (bytes[offset + 3]) & 0xff) << 32;
        value |= (bytes[offset + 4] & 0xff) << 24;
        value |= (bytes[offset + 5] & 0xff) << 16;
        value |= (bytes[offset + 6] & 0xff) << 8;
        value |= (bytes[offset + 7] & 0xff);
        return value;
    }

    private static int indexLength() {
        return toByte(0).length * 2;
    }

    private static int headerLength() {
        return toByte(0).length;
    }

    private void writeFileHeader(MappedByteBuffer mappedByteBuffer, long latestNewIndex) {
        mappedByteBuffer.position(0);
        mappedByteBuffer.putLong(latestNewIndex);
    }

    private long readFileHeader(MappedByteBuffer mappedByteBuffer) {
        mappedByteBuffer.position(0);
        return mappedByteBuffer.getLong();
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

    public static class Offset {
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

        @Override
        public String toString() {
            return "Offset{" +
                    "logFileName='" + logFileName + '\'' +
                    ", physicsOffset=" + physicsOffset +
                    '}';
        }

    }

    public Offset findOffset(long index) {
        if (index >= curIndexFileStart.get() && index <= curIndexFileEnd.get()) {
            mappedByteBuffer.get().position((int) ((index - curIndexFileStart.get()) * indexLength() + headerLength()));
            byte[] bytes = new byte[16];
            mappedByteBuffer.get().get(bytes, 0, 16);
            long offset = toLong(bytes, 8);
            if (offset >= 0) {
                return new Offset(curFile.get().split("\\.")[0], offset);
            } else if (offset == Long.MIN_VALUE) {
                // 索引被标志为删除Long.MIN_VALUE
                return null;
            }
            return null;
        }
        Postion postion = postionByIndex(index);
        long offset = -1;
        if (postion != null) {
            try (FileChannel fileChannel = FileChannel.open(Paths.get(URI.create("file:" + rootPath + "/" + postion.fileName)), StandardOpenOption.READ)) {
                fileChannel.position((index - postion.startIndex) * indexLength() + headerLength());
                ByteBuffer buffer = ByteBuffer.allocate(8);
                for (; ; ) {
                    if (fileChannel.read(buffer) != 8) {
                        break;
                    }
                    buffer.flip();
                    long indexRead = buffer.getLong();
                    buffer.clear();
                    if (fileChannel.read(buffer) != 8) {
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
    public synchronized Offset latestEffectiveIndexOffset() {
        List<String> files = listSortFiles();
        if (!files.isEmpty()) {
            String fileName = files.get(0);
            try (FileChannel channel = FileChannel.open(Paths.get(URI.create("file:" + rootPath + "/" + fileName)), StandardOpenOption.WRITE, StandardOpenOption.READ)) {
                MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, MAX_RECORD * indexLength());
                long index = readFileHeader(mappedByteBuffer);
                byte[] bytes = new byte[16];
                mappedByteBuffer.position((int) (index * indexLength() + headerLength()));
                mappedByteBuffer.get(bytes, 0, 16);
                long offset = toLong(bytes, 8);
                if (index >= 0 && offset >= 0) {
                    return new Offset(fileName.split("\\.")[0], offset);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void close() {
        if (mappedByteBuffer != null) {
            mappedByteBuffer.get().force();
            mappedByteBuffer.set(null);
            force.set(false);
        }
    }

}
