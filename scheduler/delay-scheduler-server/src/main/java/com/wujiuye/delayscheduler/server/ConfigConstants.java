package com.wujiuye.delayscheduler.server;

import com.wujiuye.delayscheduler.server.config.ApplicationConfig;
import com.wujiuye.delayscheduler.server.config.ServiceConfig;

/**
 * @author wujiuye 2021/01/21
 */
public class ConfigConstants {

    private volatile static ApplicationConfig CONFIG;

    static void setApplicationConfig(ApplicationConfig config) {
        CONFIG = config;
    }

    public static ServiceConfig getServiceConfig() {
        return CONFIG.getServer();
    }

}
