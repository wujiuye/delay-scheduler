package com.github.wujiuye.raft;

import com.github.wujiuye.raft.common.file.IndexFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IndexFileTest {

    private IndexFile indexFile;
    private ExecutorService executorService = Executors.newFixedThreadPool(50);

    @Before
    public void before() throws IOException {
        File file = new File("/tmp/test/index");
        if (!file.exists()) {
            file.mkdirs();
        }
        indexFile = new IndexFile("/tmp/test/index", "index");
    }

    @Test
    public void test() {
        for (int i = 0; i < 100; i++) {
            new AppendTask(i, i * 8).run();
            if (i % 5 == 0) {
                executorService.execute(new UpdateTask(i, -1));
            }
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
        System.out.println("====> " + indexFile.latestEffectiveIndexOffset());
        for (int i = 0; i < 100; i++) {
            IndexFile.Offset offset = indexFile.findOffset(i);
            System.out.println(i + "=>" + (offset == null ? -1 : offset.getPhysicsOffset()));
        }
    }

    private class AppendTask implements Runnable {

        private long index;
        private long offset;

        public AppendTask(long index, long offset) {
            this.index = index;
            this.offset = offset;
        }

        @Override
        public void run() {
            try {
                indexFile.appendOffset(index, offset);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class UpdateTask implements Runnable {

        private long index;
        private long offset;

        public UpdateTask(long index, long offset) {
            this.index = index;
            this.offset = offset;
        }

        @Override
        public void run() {
            try {
                indexFile.updateOffset(index, offset);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @After
    public void after() throws IOException {
        executorService.shutdownNow();
        indexFile.close();
        File file = new File("/tmp/test/index");
        if (file.exists()) {
            File[] childList = file.listFiles();
            for (File file1 : childList) {
                file1.delete();
            }
            file.delete();
        }
    }

}
