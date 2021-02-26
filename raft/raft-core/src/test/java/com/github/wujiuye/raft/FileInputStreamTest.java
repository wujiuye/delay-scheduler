package com.github.wujiuye.raft;

import org.junit.Test;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FileInputStreamTest {

    @Test
    public void testRead() throws IOException {
        try (FileInputStream fis = new FileInputStream("/tmp/test/test.log")) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int byteData;
            while ((byteData = fis.read()) != -1) {
                if (byteData == '\n') {
                    buffer.flip();
                    String line = new String(buffer.array(), buffer.position(), buffer.limit());
                    System.out.println(line);
                    buffer.clear();
                    continue;
                }
                buffer.put((byte) byteData);
            }
        }
    }

}
