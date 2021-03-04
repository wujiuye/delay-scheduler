package com.github.wujiuye.raft.demo;

import com.github.wujiuye.raft.appender.CommandLogAppender;
import com.github.wujiuye.raft.appender.DefaultCommandLogAppender;
import com.github.wujiuye.raft.appender.FileCommandLogAppender;
import com.github.wujiuye.raft.common.NodeIpPort;
import com.github.wujiuye.raft.common.SignalManager;
import com.github.wujiuye.raft.rpc.CommandResp;
import com.github.wujiuye.raft.rpc.RaftCommandClient;
import com.github.wujiuye.raft.sdk.RaftNodeBootstarter;
import com.github.wujiuye.raft.StateMachine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * @author wujiuye 2020/12/16
 */
public class RaftNodeMain {

    private static Set<NodeIpPort> allNodes() {
        Set<NodeIpPort> nodeIpPorts = new HashSet<>();
        nodeIpPorts.add(new NodeIpPort(1, "127.0.0.1", 8090));
        nodeIpPorts.add(new NodeIpPort(2, "127.0.0.1", 8091));
        nodeIpPorts.add(new NodeIpPort(3, "127.0.0.1", 8092));
        return nodeIpPorts;
    }

    private static StateMachine stateMachine() {
        return new PrintCommandStateMachine();
    }

    private static CommandLogAppender createDefaultCommandLogAppender() {
        return new DefaultCommandLogAppender();
    }

    private static CommandLogAppender createFileCommandLogAppender() {
        try {
            FileCommandLogAppender appender = new FileCommandLogAppender("/tmp/raft/demo");
            SignalManager.registToLast(signal -> {
                try {
                    appender.close();
                } catch (IOException ignored) {
                }
            });
            return appender;
        } catch (IOException e) {
            System.exit(-1);
            return null;
        }
    }

    /**
     * 命令行启动
     * java -jar raft-demo nodeId
     *
     * @param args args[0]为nodeId
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        int nodeId = Integer.parseInt(args[0]);
        RaftCommandClient raftCommandClient = RaftNodeBootstarter.bootstartRaftNode(
                nodeId,  // cur node id
                allNodes(), // all node
                stateMachine(), // state machine
                createFileCommandLogAppender() // log appender
        );
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String str;
        while (!"q".equals(str = br.readLine())) {
            byte[] command = ("save {\"taskName\":\"payCallback\",\"param\":" + str + "}").getBytes();
            try {
                // 模拟发送命令
                CommandResp commandResp = raftCommandClient.handleCommand(command);
                System.out.println(commandResp);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

}
