package com.github.wujiuye.raft.appender;

import com.github.wujiuye.raft.CommandLog;

/**
 * 日记写入
 * 注意：需确保并发安全
 *
 * @author wujiuye 2020/12/15
 */
public interface CommandLogAppender {

    /**
     * 获取最近写入的一个日记条目
     *
     * @return
     */
    CommandLog peek();

    /**
     * 获取索引位置处的条目
     *
     * @param index 日记索引
     * @return
     */
    CommandLog index(long index);

    /**
     * 追加日记
     *
     * @param commandLog 日记
     */
    void append(CommandLog commandLog);

    /**
     * 更新
     *
     * @param commandLog
     */
    void update(CommandLog commandLog);

    /**
     * 删除startIndex开始的日记
     *
     * @param term       期号
     * @param startIndex 日记index
     */
    void removeRange(long term, long startIndex);

    CommandLog[] range(long startIndex, long endIndex);

}
