package com.wujiuye.delayscheduler.server.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

import java.lang.reflect.Proxy;

/**
 * @author wujiuye 2021/01/08
 */
public class LoggerUtils {

    private final static Logger logger;

    static {
        Logger tmp = LoggerFactory.getLogger("delay-scheduler-server");
        if (tmp instanceof NOPLogger) {
            tmp = (Logger) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class<?>[]{Logger.class},
                    (proxy, method, args) -> {
                        if ("error".equalsIgnoreCase(method.getName())) {
                            System.err.println(formatMessage(args));
                        } else {
                            System.out.println(formatMessage(args));
                        }
                        return null;
                    });
        }
        logger = tmp;
    }

    private static String formatMessage(Object[] args) {
        String pmsg = (String) args[0];
        if (args.length > 1) {
            pmsg = pmsg.replace("{}", "%s");
            Object[] param = new Object[args.length - 1];
            System.arraycopy(args, 1, param, 0, param.length);
            pmsg = String.format(pmsg, param);
        }
        return pmsg;
    }

    public static Logger getLogger() {
        return logger;
    }

}
