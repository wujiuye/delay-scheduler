package com.wujiuye.delayscheduler.server.common;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * IP工具类
 *
 * @author wujiuye 2020/09/14
 */
public class IpAndProcessIdUtils {

    private final static String LOCAL_ADDRESS;
    private final static int PID;

    static {
        String hostAddress;
        try {
            hostAddress = getCurrentIp();
        } catch (Exception e) {
            hostAddress = "";
        }
        LOCAL_ADDRESS = hostAddress;
        PID = getProcessID();
    }

    /**
     * 获取本机在内网的IP地址
     *
     * @return
     */
    public static String getLocalAddress() {
        return LOCAL_ADDRESS;
    }

    private static String getCurrentIp() throws SocketException {
        // 得到当前机器上在局域网内所有的网络接口
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        // 遍历所有的网络接口
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface ni = networkInterfaces.nextElement();
            // 获取当前接口下绑定到该网卡的所有的IP地址。
            Enumeration<InetAddress> nias = ni.getInetAddresses();
            while (nias.hasMoreElements()) {
                InetAddress ia = nias.nextElement();
                // 获取网卡接口IP地址
                String hostAddress = ia.getHostAddress();
                // 排除ipv6地址和127.0.0.1 取ipv4地址
                if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress() && ia instanceof Inet4Address) {
                    return hostAddress;
                }
            }
        }
        return null;
    }

    public static int getPid() {
        return PID;
    }

    private static int getProcessID() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return Integer.valueOf(runtimeMXBean.getName().split("@")[0]).intValue();
    }

}
