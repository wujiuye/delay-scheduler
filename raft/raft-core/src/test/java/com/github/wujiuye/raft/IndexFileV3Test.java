package com.github.wujiuye.raft;

import com.github.wujiuye.raft.common.file.IndexFileV3;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IndexFileV3Test {

    private IndexFileV3 indexFileV3;
    private ExecutorService executorService = Executors.newFixedThreadPool(50);

    @Before
    public void before() throws IOException {
        File file = new File("/tmp/test/index_v3");
        if (!file.exists()) {
            file.mkdirs();
        }
        indexFileV3 = new IndexFileV3("/tmp/test/index_v3", "index");
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
        System.out.println("====> " + indexFileV3.latestEffectiveIndexOffset());
        for (int i = 0; i < 100; i++) {
            IndexFileV3.Offset offset = indexFileV3.findOffset(i);
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
                indexFileV3.appendOffset(index, offset);
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
                indexFileV3.updateOffset(index, offset);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @After
    public void after() throws IOException {
        executorService.shutdownNow();
        indexFileV3.close();
        File file = new File("/tmp/test/index_v3");
        if (file.exists()) {
            File[] childList = file.listFiles();
            for (File file1 : childList) {
                file1.delete();
            }
            file.delete();
        }
    }

}
