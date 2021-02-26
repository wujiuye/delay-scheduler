package com.github.wujiuye.raft;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MappedByteBufferTest {

    @Before
    public void before() throws IOException {
        File file = new File("/tmp/test/test.log");
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    @Test
    public void testMappedByteBuffer() throws IOException {
        FileChannel fileChannel = FileChannel.open(Paths.get(URI.create("file:/tmp/test/test.log")),
                StandardOpenOption.WRITE, StandardOpenOption.READ);
        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 1024, 4096);
        fileChannel.close();
        mappedByteBuffer.position(1024);
        mappedByteBuffer.putLong(10000L);
        mappedByteBuffer.force();
    }

    @Test
    public void testMappedByteBufferOnlyRead() throws IOException {
        FileChannel fileChannel = FileChannel.open(Paths.get(URI.create("file:/tmp/test/test.log")),
                StandardOpenOption.READ);
        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, 4096);
        fileChannel.close();
        mappedByteBuffer.position(1024);
        long value = mappedByteBuffer.getLong();
        System.out.println(value);
    }

}
