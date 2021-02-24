package com.github.wujiuye.transport.netty.protocol.message.heartbeat;

import com.github.wujiuye.transport.netty.protocol.message.Message;
import com.github.wujiuye.transport.netty.protocol.message.MessageTypes;
import com.github.wujiuye.transport.netty.protocol.message.MessageVersions;

/**
 * @author wujiuye
 * @version 1.0 on 2020/03/11 心跳包请求包
 * }
 */
public class Heartbeat implements Message {

    /**
     * 进程ID
     */
    private int pid;

    @Override
    public Byte getType() {
        return MessageTypes.MSG_TYPE_PING;
    }

    @Override
    public Byte getVersion() {
        return MessageVersions.VERSION;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    @Override
    public String toString() {
        return "Heartbeat{" +
                "pid=" + pid +
                '}';
    }

}
