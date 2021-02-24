package com.github.wujiuye.raft.common;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 全局ID生成器
 *
 * @author wujiuye 2021/01/08
 */
public class IdUtils {

    private static final AtomicInteger index = new AtomicInteger();

    public static int newId() {
        return index.getAndIncrement();
    }

}
