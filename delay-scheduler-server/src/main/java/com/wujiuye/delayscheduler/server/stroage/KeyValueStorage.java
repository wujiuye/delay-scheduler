package com.wujiuye.delayscheduler.server.stroage;

import com.wujiuye.delayscheduler.core.ActionLog;

import java.io.Closeable;

/**
 * @author wujiuye 2021/01/12
 */
public interface KeyValueStorage extends Closeable {

    /**
     * 保存
     *
     * @param application 名称空间
     * @param key
     * @param value
     */
    void save(String application, String key, ActionLog value);

    /**
     * 获取
     *
     * @param application 名称空间
     * @param key
     * @return
     */
    ActionLog get(String application, String key);

    /**
     * 删除
     *
     * @param application 名称空间
     * @param key
     */
    void del(String application, String key);

}
