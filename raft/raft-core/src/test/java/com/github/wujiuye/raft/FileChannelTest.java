package com.github.wujiuye.raft;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileChannelTest {

    private FileChannel fileChannel;
    private ExecutorService executorService;

    @Before
    public void before() throws IOException {
        File file = new File("/tmp/test/test.log");
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        fileChannel = FileChannel.open(Paths.get(URI.create("file:/tmp/test/test.log")), StandardOpenOption.WRITE, StandardOpenOption.READ);
        executorService = Executors.newFixedThreadPool(50);
    }

    @Test
    public void testWrite() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            final int v = i;
            executorService.execute(() -> {
                byte[] info1 = ("v" + v).getBytes();
                byte[] info2 = ("y" + v).getBytes();
                try {
                    // 这三步要加锁
                    // synchronized (FileChannelTest.class) {
                    fileChannel.write(ByteBuffer.wrap(info1));
                    fileChannel.write(ByteBuffer.wrap(info2));
                    fileChannel.write(ByteBuffer.wrap("\n".getBytes()));
                    //}
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
    }

    @After
    public void after() throws IOException {
        executorService.shutdownNow();
        fileChannel.force(true);
        fileChannel.close();
    }

}
