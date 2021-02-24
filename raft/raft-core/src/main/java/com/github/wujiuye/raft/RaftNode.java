package com.github.wujiuye.raft;

import com.github.wujiuye.raft.common.NodeIpPort;
import com.github.wujiuye.raft.common.TermVoter;
import com.github.wujiuye.raft.common.TermVoterHolder;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Raft节点
 *
 * @author wujiuye 2020/12/15
 */
public class RaftNode {

    /**
     * 节点ID
     */
    private Integer id;
    /**
     * 节点的当前期号
     */
    private AtomicLong curTerm = new AtomicLong(0);
    /**
     * 节点角色，刚启动时设置为候选人
     */
    private AtomicReference<RaftNodeRole> raftNodeRole = new AtomicReference<>(RaftNodeRole.Candidate);
    /**
     * 当前期选举得票总数
     */
    private AtomicInteger voteNumber = new AtomicInteger(0);
    /**
     * 记录已提交日记条目的索引
     */
    private AtomicLong commitIndex = new AtomicLong(0);

    private AtomicReference<NodeIpPort> nodeIpPort = new AtomicReference<>();

    private TermVoterHolder termVoterHolder = new TermVoterHolder();

    public TermVoterHolder getTermVoterHolder() {
        return termVoterHolder;
    }

    public RaftNode(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public long getCurTerm() {
        return curTerm.longValue();
    }

    public RaftNodeRole getRaftNodeRole() {
        return raftNodeRole.get();
    }

    public void setNodeIpPort(NodeIpPort nodeIpPort) {
        this.nodeIpPort.set(nodeIpPort);
    }

    public NodeIpPort getNodeIpPort() {
        return nodeIpPort.get();
    }

    /**
     * 初始化期号
     *
     * @param term 当前期（从commandLog获取）
     */
    public void initCurTerm(long term) {
        this.curTerm.compareAndSet(0, term);
    }

    /**
     * 重置到新的一期开始选举状态
     *
     * @return true:获取到自己的选票，false:没获取到自己的选票
     */
    public synchronized boolean restNewTermVote() {
        // 变为候选人
        this.raftNodeRole.set(RaftNodeRole.Candidate);
        long curTerm = this.curTerm.incrementAndGet();
        TermVoter termVoter = termVoterHolder.getTermVoter(curTerm);
        if (termVoter.lockVote(this.id)) {
            // 给自己投一票
            this.voteNumber.set(1);
            return true;
        }
        return false;
    }

    /**
     * 选举得票+1
     */
    public void incVoteNumer() {
        this.voteNumber.incrementAndGet();
    }

    /**
     * 获取当前期的得票总数
     *
     * @return
     */
    public int getVoteNumber() {
        return voteNumber.intValue();
    }

    /**
     * 将节点置为Follower节点
     */
    public void asFollower(long leaderTerm) {
        if (leaderTerm >= getCurTerm()) {
            this.raftNodeRole.set(RaftNodeRole.Follower);
            this.curTerm.set(leaderTerm);
        }
    }

    /**
     * 将节点置为Leader节点
     */
    public void asLeader(long leaderTerm) {
        if (leaderTerm >= getCurTerm()) {
            this.raftNodeRole.set(RaftNodeRole.Leader);
            this.curTerm.set(leaderTerm);
            for (RaftNode raftNode : Raft.getAllNodes()) {
                if (raftNode.getId().equals(id)) {
                    continue;
                }
                raftNode.asFollower(leaderTerm);
            }
        }
    }

    /**
     * 索引跃进(乐观跃进，不需要加锁)
     *
     * @param newCommitIndex 新的commit值
     */
    public void seekCommitIndex(long newCommitIndex) {
        this.commitIndex.set(newCommitIndex);
    }

    public long curCommitIndex() {
        return commitIndex.longValue();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof RaftNode)) {
            return false;
        }
        RaftNode node = (RaftNode) object;
        return Objects.equals(getId(), node.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "RaftNode{" +
                "id=" + id +
                ", curTerm=" + curTerm.longValue() +
                ", raftNodeRole=" + raftNodeRole.get() +
                ", voteNumber=" + voteNumber.intValue() +
                ", commitIndex=" + commitIndex.longValue() +
                ", nodeIpPort=" + nodeIpPort.get() +
                '}';
    }

}
