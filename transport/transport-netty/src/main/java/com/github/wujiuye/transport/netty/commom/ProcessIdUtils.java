package com.github.wujiuye.transport.netty.commom;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * IP工具类
 *
 * @author wujiuye 2020/09/14
 */
public class ProcessIdUtils {

    private final static int PID;

    static {
        PID = getProcessID();
    }

    public static int getPid() {
        return PID;
    }

    private static int getProcessID() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return Integer.valueOf(runtimeMXBean.getName().split("@")[0]).intValue();
    }

}
