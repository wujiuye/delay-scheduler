package com.wujiuye.delayscheduler.server;

import com.github.wujiuye.raft.rpc.RaftCommandClient;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author wujiuye 2021/01/12
 */
public class RaftConstants {

    private final static AtomicReference<RaftCommandClient> RAFT_COMMAND_CLIENT_REF = new AtomicReference<>(null);

    public static void holdRaftCommandClient(RaftCommandClient raftCommandClient) {
        RAFT_COMMAND_CLIENT_REF.set(raftCommandClient);
    }

    public static RaftCommandClient getRaftCommandClient() {
        return RAFT_COMMAND_CLIENT_REF.get();
    }

}
