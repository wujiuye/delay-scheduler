package com.github.wujiuye.raft.common;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;

/**
 * @author wujiuye 2020/11/13
 */
public class SignalManager {

    private final static LinkedList<SignalHandler> SIGNAL_HANDLERS = new LinkedList<>();

    public static void registToLast(Closeable... closeables) {
        synchronized (SIGNAL_HANDLERS) {
            for (Closeable c : closeables) {
                SIGNAL_HANDLERS.addLast(signal -> {
                    try {
                        c.close();
                    } catch (IOException ignored) {
                    }
                });
            }
        }
    }

    public static void registToFirst(Closeable... closeables) {
        synchronized (SIGNAL_HANDLERS) {
            for (Closeable c : closeables) {
                SIGNAL_HANDLERS.addLast(signal -> {
                    try {
                        c.close();
                    } catch (IOException ignored) {
                    }
                });
            }
        }
    }

    /**
     * 注册信号量处理器
     *
     * @param signalHandler 信号量处理器
     */
    public static void registToLast(SignalHandler signalHandler) {
        synchronized (SIGNAL_HANDLERS) {
            SIGNAL_HANDLERS.addLast(signalHandler);
        }
    }

    public static void registToFirst(SignalHandler signalHandler) {
        synchronized (SIGNAL_HANDLERS) {
            SIGNAL_HANDLERS.addFirst(signalHandler);
        }
    }

    private static void showLink() {
        SIGNAL_HANDLERS.parallelStream().forEach(signalHandler -> LoggerUtils.getLogger().debug("signalHandler is {}",
                signalHandler.getClass().getName()));
    }

    static {
        SignalHandler signalHandler = signal -> {
            synchronized (SIGNAL_HANDLERS) {
                if (!SIGNAL_HANDLERS.isEmpty()) {
                    showLink();
                    SIGNAL_HANDLERS.forEach(handle -> {
                        try {
                            handle.handle(signal);
                        } catch (Throwable ignored) {
                        }
                    });
                }
            }
        };
        Signal.handle(new Signal("INT"), signalHandler);
    }

}
