package com.wujiuye.delayscheduler.server.stroage;

import com.wujiuye.delayscheduler.server.common.LoggerUtils;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBComparator;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;

/**
 * @author wujiuye 2021/01/20
 */
public class Leveldb {

    private volatile static DB database;

    /**
     * 自定义key排序，方便seek搜索
     */
    private final static DBComparator DB_COMPARATOR = new DBComparator() {
        @Override
        public String name() {
            // 名称只能是这个
            return "leveldb.BytewiseComparator";
        }

        @Override
        public byte[] findShortestSeparator(byte[] start, byte[] limit) {
            return start;
        }

        @Override
        public byte[] findShortSuccessor(byte[] key) {
            return key;
        }

        @Override
        public int compare(byte[] o1, byte[] o2) {
            String key1 = new String(o1);
            String key2 = new String(o2);
            return key1.compareTo(key2);
        }
    };

    private static File ensureDirExist(String dataRootPath) {
        File file = new File(dataRootPath);
        if (file.exists()) {
            if (file.isDirectory()) {
                return file;
            }
            file.delete();
        }
        file.mkdirs();
        return file;
    }

    public static synchronized void openDb(String dataRootPath) {
        if (database == null) {
            DBFactory factory = new JniDBFactory();
            Options options = new Options();
            options.createIfMissing(true);
            options.comparator(DB_COMPARATOR);
            try {
                database = factory.open(ensureDirExist(dataRootPath), options);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("database already open and only open one!");
        }
    }

    public static DB getDatabase() {
        return database;
    }

    public static synchronized void closeDb() {
        final DB ref = database;
        database = null;
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            }
            LoggerUtils.getLogger().debug("leveldb close...");
            try {
                synchronized (Leveldb.class) {
                    ref.close();
                }
            } catch (IOException ignored) {
            }
        }).start();
    }

}
