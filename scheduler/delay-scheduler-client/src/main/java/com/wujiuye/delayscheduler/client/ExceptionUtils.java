package com.wujiuye.delayscheduler.client;

/**
 * @author wujiuye 2021/02/05
 */
public class ExceptionUtils {

    public static Throwable getCaused(Throwable ex) {
        if (ex == null) {
            return null;
        }
        Throwable caused = ex;
        for (; caused.getCause() != null; caused = caused.getCause()) {
        }
        return caused;
    }

    public static RuntimeException warpThrowable(Throwable ex) {
        Throwable caused = getCaused(ex);
        if (caused instanceof RuntimeException) {
            return (RuntimeException) caused;
        }
        return new RuntimeException(ex);
    }

}
