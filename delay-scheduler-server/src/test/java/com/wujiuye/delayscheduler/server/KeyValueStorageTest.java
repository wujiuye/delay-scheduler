package com.wujiuye.delayscheduler.server;

import com.wujiuye.delayscheduler.core.ActionLog;
import com.wujiuye.delayscheduler.server.stroage.Leveldb;
import com.wujiuye.delayscheduler.server.stroage.LeveldbKeyValueStorage;
import com.wujiuye.delayscheduler.server.stroage.KeyValueStorage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class KeyValueStorageTest {

    private KeyValueStorage storage1;

    @Before
    public void before() {
        Leveldb.openDb("/tmp/leveldb");
        storage1 = new LeveldbKeyValueStorage();
    }

    @After
    public void after() throws IOException {
        storage1.close();
    }

    @Test
    public void testDb() {
        storage1.save("test1", "testkey1", new ActionLog());
    }

}
