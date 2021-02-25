package com.wujiuye.delayscheduler.server.stroage;

import com.github.wujiuye.transport.netty.commom.JsonUtils;
import com.wujiuye.delayscheduler.core.ActionLog;
import com.wujiuye.delayscheduler.core.ActionStat;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBException;

/**
 * ActionLog key-value存储 key为索引
 *
 * @author wujiuye 2021/01/12
 */
public class LeveldbKeyValueStorage implements KeyValueStorage {

    private DB getDatabase() {
        DB db = Leveldb.getDatabase();
        if (db == null) {
            throw new DBException("db not open.");
        }
        return db;
    }

    private byte[] getKey(String application, String key, String stat) {
        return (stat + "::" + application + "::" + key).getBytes();
    }

    @Override
    public void save(String application, String key, ActionLog value) {
        getDatabase().put(getKey(application, key, value.getStatus()), JsonUtils.toJsonString(value).getBytes());
        del(application, key, ActionStat.valueBy(value.getStatus()));
    }

    @Override
    public ActionLog get(String application, String key) {
        byte[] bytes = null;
        for (ActionStat stat : ActionStat.values()) {
            bytes = getDatabase().get(getKey(application, key, stat.getStat()));
            if (bytes != null) {
                break;
            }
        }
        return bytes == null ? null : JsonUtils.fromJson(new String(bytes), ActionLog.class);
    }

    @Override
    public void del(String application, String key) {
        del(application, key, null);
    }

    private void del(String application, String key, ActionStat excludeStat) {
        for (ActionStat stat : ActionStat.values()) {
            if (excludeStat != null && stat == excludeStat) {
                continue;
            }
            getDatabase().delete(getKey(application, key, stat.getStat()));
        }
    }

    @Override
    public void close() {

    }

}
