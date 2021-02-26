package com.github.wujiuye.raft;

import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;

public class FileOutputStreamTest {

    @Test
    public void testWrite() throws IOException {
        try (FileOutputStream fos = new FileOutputStream("/tmp/test/test.log")) {
//            fos.write();
//            fos.flush();
        }
    }

}
