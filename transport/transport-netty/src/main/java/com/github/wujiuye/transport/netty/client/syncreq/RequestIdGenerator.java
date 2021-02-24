package com.github.wujiuye.transport.netty.client.syncreq;

import java.util.UUID;

/**
 * 获取请求ID
 *
 * @author wujiuye 2020/10/13
 */
public class RequestIdGenerator {

    /**
     * 生成请求ID
     *
     * @return
     */
    public static String generatorRequestId() {
        return UUID.randomUUID().toString();
    }

}
