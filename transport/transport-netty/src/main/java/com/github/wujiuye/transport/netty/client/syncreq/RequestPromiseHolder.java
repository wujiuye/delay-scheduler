package com.github.wujiuye.transport.netty.client.syncreq;

import com.github.wujiuye.transport.netty.protocol.message.Message;
import io.netty.channel.ChannelPromise;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求持有者
 *
 * @author wujiuye 2020/10/12
 */
public final class RequestPromiseHolder {

    private RequestPromiseHolder() {
    }

    private static final Map<String, AbstractMap.SimpleEntry<ChannelPromise, Message>> PROMISE_MAP
            = new ConcurrentHashMap<>();

    public static void putPromise(String tid, ChannelPromise promise) {
        PROMISE_MAP.put(tid, new AbstractMap.SimpleEntry<>(promise, null));
    }

    public static AbstractMap.SimpleEntry<ChannelPromise, Message> getEntry(String tid) {
        return PROMISE_MAP.get(tid);
    }

    public static void remove(String tid) {
        PROMISE_MAP.remove(tid);
    }

    public static void completePromise(String tid, Message response) {
        if (!PROMISE_MAP.containsKey(tid)) {
            return;
        }
        AbstractMap.SimpleEntry<ChannelPromise, Message> entry = PROMISE_MAP.get(tid);
        if (entry != null) {
            ChannelPromise promise = entry.getKey();
            if (promise.isDone() || promise.isCancelled()) {
                return;
            }
            entry.setValue(response);
            promise.setSuccess();
        }
    }

}
