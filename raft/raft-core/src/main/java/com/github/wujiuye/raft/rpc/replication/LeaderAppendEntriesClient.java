package com.github.wujiuye.raft.rpc.replication;

import com.github.wujiuye.raft.*;
import com.github.wujiuye.raft.appender.CommandLogAppender;
import com.github.wujiuye.raft.common.CountWaiter;
import com.github.wujiuye.raft.common.IdUtils;
import com.github.wujiuye.raft.common.LoggerUtils;
import com.github.wujiuye.raft.CommandLog;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Leader节点调用，向Follower节点发送复制请求
 *
 * @author wujiuye 2020/12/16
 */
public class LeaderAppendEntriesClient implements Closeable {

    private int nodeId;
    private RemoteRouter<AppendEntriesRpc> remoteRouter;
    private CommandLogAppender commandLogAppender;

    private final ExecutorService commandExecutorService;
    private final ExecutorService heartbeatExecutorService;

    private static ExecutorService newExecutorService(String name, int threads, int queueCap) {
        return new ThreadPoolExecutor(threads, threads,
                60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueCap),
                r -> new Thread(r, name + "-" + IdUtils.newId()),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public LeaderAppendEntriesClient(int nodeId, RemoteRouter<AppendEntriesRpc> remoteRouter, CommandLogAppender commandLogAppender) {
        this.nodeId = nodeId;
        this.remoteRouter = remoteRouter;
        this.commandLogAppender = commandLogAppender;
        commandExecutorService = newExecutorService("command", Raft.nodeCount(), 1);
        heartbeatExecutorService = newExecutorService("heartbeat", Raft.nodeCount(), 1);
    }

    /**
     * 向Follower节点发送复制请求，成功复制到多数节点后就立即响应true
     *
     * @param appendEntries 日记条目（命令）
     * @return true:提交成功 false:提交失败
     */
    public synchronized boolean appendCommand(AppendEntries appendEntries) {
        Set<RaftNode> nodes = Raft.getAllNodes();
        final CountWaiter countWaiter = new CountWaiter(Raft.nodeCount() - 1);
        for (RaftNode node : nodes) {
            if (node.getId().equals(nodeId)) {
                continue;
            }
            commandExecutorService.execute(() -> {
                RaftNode toNode = Raft.getNode(node.getId());
                AppendEntries toAppendEntries = appendEntries.clone();
                toAppendEntries.setLeaderCommit(toNode.curCommitIndex());
                try {
                    AppendEntriesResp resp = remoteRouter.routeRpc(node.getNodeIpPort()).appendCommand(appendEntries);
                    LoggerUtils.getLogger().debug("appendCommand node:{}, resp:{}", toNode.getId(), resp);
                    if (resp.isSuccess()) {
                        countWaiter.countDownSuccess();
                    } else if (resp.getTerm() <= Raft.getNode(nodeId).getCurTerm()) {
                        // 在失败之后，领导人会将nextIndex递减（nextIndex），然后重试AppendEntriesRPC，直到AppendEntriesRPC返回成功为止。
                        // 这才表明在nextIndex位置的日志条目中领导人与追随者的保持一致。
                        // 这时，Follower上nextIndex位置之前的日志条目将全部保留，在此之后（与Leader有冲突）的日志条目将被Follower全部删除，
                        // 并且从该位置起追加Leader上在nextIndex位置之后的所有日志条目。
                        // 因此，一旦AppendEntriesRPC返回成功，Leader和Follower的日志就可以保持一致了。
                        long endIndex = toAppendEntries.getPrevLogIndex();
                        long nextIndex = endIndex - 1;
                        while (nextIndex >= -1) {
                            if (nextIndex == -1) {
                                // 从头开始同步
                                toAppendEntries.setPrevLogTerm(-1);
                                toAppendEntries.setPrevLogIndex(-1);
                                toAppendEntries.setEntries(commandLogAppender.range(0, endIndex));
                            } else {
                                CommandLog commandLog = commandLogAppender.index(nextIndex);
                                toAppendEntries.setPrevLogTerm(commandLog.getTerm());
                                toAppendEntries.setPrevLogIndex(commandLog.getIndex());
                                toAppendEntries.setEntries(commandLogAppender.range(nextIndex, endIndex));
                            }
                            resp = remoteRouter.routeRpc(node.getNodeIpPort()).appendCommand(toAppendEntries);
                            LoggerUtils.getLogger().debug("batch sync appendCommand [{}]. node:{}, req:{}, resp:{}",
                                    resp.isSuccess(), toNode.getId(), toAppendEntries, resp);
                            if (resp.isSuccess()) {
                                // 同步之后再追加当前提交
                                resp = remoteRouter.routeRpc(node.getNodeIpPort()).appendCommand(appendEntries);
                                LoggerUtils.getLogger().debug("retry appendCommand node:{}, resp:{}", toNode.getId(), resp);
                                if (resp.isSuccess()) {
                                    countWaiter.countDownSuccess();
                                }
                                break;
                            }
                            nextIndex--;
                        }
                        // 自己过期了 (由心跳去处理，此处不处理)
                    }
                } catch (Throwable throwable) {
                    countWaiter.countDownException();
                    LoggerUtils.getLogger().warn("append command to node {} error,error msg:{} ", toNode.getId(), throwable.getMessage());
                } finally {
                    countWaiter.countDown();
                }
            });
        }
        int count = Raft.nodeCount();
        int duoshuNode = (count / 2) + 1;
        // 等待多数节点响应成功(duoshuNode - 1: 包括自己)
        // countWaiter.await(duoshuNode - 1);
        // 等待所有节点完成，避免并发请求
        countWaiter.await();
        // 多数节点复制成功
        return countWaiter.successCnt() + 1 >= duoshuNode;
    }

    /**
     * 同步提交给Follower节点
     *
     * @param commandLog 日记条目
     */
    public void commit(CommandLog commandLog) {
        commandExecutorService.execute(() -> {
            for (RaftNode raftNode : Raft.getAllNodes()) {
                if (raftNode.getId() == nodeId) {
                    continue;
                }
                try {
                    if (remoteRouter.routeRpc(raftNode.getNodeIpPort()).commit(commandLog.getTerm(), commandLog.getIndex())) {
                        // 领导节点记录每个Follower节点的已提交commitIndex
                        raftNode.seekCommitIndex(commandLog.getIndex());
                    }
                } catch (Throwable throwable) {
                    LoggerUtils.getLogger().warn("append command to node {} error,error msg:{} ",
                            raftNode.getId(), throwable.getMessage());
                }
            }
        });
    }

    /**
     * 向所有Follower节点发送心跳包
     */
    public boolean sendHeartbeatCommand() {
        CountWaiter countWaiter = new CountWaiter(Raft.nodeCount() - 1);
        AtomicBoolean success = new AtomicBoolean(Boolean.TRUE);
        for (RaftNode raftNode : Raft.getAllNodes()) {
            // 跳过自己
            if (raftNode.getId() == nodeId) {
                continue;
            }
            heartbeatExecutorService.execute(() -> {
                AppendEntries heartbeat = newAppendEntries();
                heartbeat.setLeaderCommit(raftNode.curCommitIndex());
                try {
                    AppendEntriesResp resp = remoteRouter.routeRpc(raftNode.getNodeIpPort()).appendCommand(heartbeat);
                    if (!resp.isSuccess() && resp.getTerm() > Raft.getNode(nodeId).getCurTerm()) {
                        // 自己已经过时了
                        success.set(Boolean.FALSE);
                    }
                } catch (Throwable throwable) {
                    // LoggerUtils.getLogger().warn("send heartbeat to node {} error,error msg:{} ",
                    //        raftNode.getId(), throwable.getMessage());
                } finally {
                    countWaiter.countDown();
                }
            });
        }
        countWaiter.await();
        return success.get();
    }

    public final AppendEntries newAppendEntries() {
        RaftNode leaderRaftNode = Raft.getNode(nodeId);
        AppendEntries appendEntries = new AppendEntries();
        appendEntries.setLeaderId(leaderRaftNode.getId());
        appendEntries.setTerm(leaderRaftNode.getCurTerm());
        // 根据领导人记录的已提交索引获取日记条目
        CommandLog commandLog = commandLogAppender.index(leaderRaftNode.curCommitIndex());
        if (commandLog == null) {
            appendEntries.setPrevLogTerm(-1);
            appendEntries.setPrevLogIndex(-1);
        } else {
            appendEntries.setPrevLogTerm(commandLog.getTerm());
            appendEntries.setPrevLogIndex(commandLog.getIndex());
        }
        return appendEntries;
    }

    @Override
    public void close() throws IOException {
        commandExecutorService.shutdown();
        heartbeatExecutorService.shutdownNow();
    }

}
