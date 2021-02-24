package com.github.wujiuye.transport.demo.server;

/**
 * @author wujiuye 2021/01/07
 */
public class ServerDemoMain {

    public static void main(String[] args) {
        TransportServerImpl.startServer(8080, new DemoRpcInvokerRouter());
    }

}
