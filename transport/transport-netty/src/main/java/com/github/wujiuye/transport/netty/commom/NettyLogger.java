package com.github.wujiuye.transport.netty.commom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

import java.lang.reflect.Proxy;

/**
 * @author wujiuye 2020/09/29
 */
public final class NettyLogger {

    private final static Logger logger;

    static {
        Logger tmp = LoggerFactory.getLogger(NettyLogger.class);
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

    public static void debug(String s) {
        logger.debug(s);
    }

    public static void debug(String s, Object... objects) {
        logger.debug(s, objects);
    }

    public static void info(String s) {
        logger.info(s);
    }

    public static void info(String s, Object... objects) {
        logger.info(s, objects);
    }

    public static void warn(String s) {
        logger.warn(s);
    }

    public static void warn(String s, Object... objects) {
        logger.warn(s, objects);
    }

    public static void error(String s) {
        logger.error(s);
    }

    public static void error(String s, Object... objects) {
        logger.error(s, objects);
    }

}
