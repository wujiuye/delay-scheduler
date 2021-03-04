package com.github.wujiuye.raft.appender;

import com.github.wujiuye.raft.CommandLog;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 仅用于测试，日记在重启时将全部丢失
 *
 * @author wujiuye 2021/01/12
 */
public class DefaultCommandLogAppender implements CommandLogAppender {

    private static class EntityNode {
        private EntityNode preNode;
        private CommandLog commandLog;

        public EntityNode(CommandLog commandLog) {
            this.commandLog = commandLog;
        }
    }

    private EntityNode endNode;
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    @Override
    public CommandLog peek() {
        readWriteLock.readLock().lock();
        try {
            return endNode == null ? null : endNode.commandLog;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public CommandLog index(long index) {
        readWriteLock.readLock().lock();
        try {
            EntityNode node = endNode;
            for (; node != null
                    && node.commandLog.getIndex() != index
                    ; node = node.preNode) {
            }
            return node == null ? null : node.commandLog;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    private void removeOut(int capSize) {
        EntityNode ptr = endNode, next = null;
        int i = 1;
        for (; ptr != null && i <= capSize; i++, next = ptr, ptr = ptr.preNode) {
        }
        if (i > capSize && next != null) {
            next.preNode = null;
        }
    }

    @Override
    public void append(CommandLog commandLog) {
        readWriteLock.writeLock().lock();
        try {
            EntityNode node = new EntityNode(commandLog);
            node.preNode = endNode;
            endNode = node;
            removeOut(10000);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void update(CommandLog commandLog) {
        readWriteLock.writeLock().lock();
        try {
            EntityNode node = endNode;
            for (; node != null
                    && node.commandLog.getIndex() != commandLog.getIndex()
                    && node.commandLog.getTerm() != commandLog.getTerm()
                    ; node = node.preNode) {
            }
            if (node != null) {
                node.commandLog = commandLog;
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void removeRange(long term, long startIndex) {
        readWriteLock.writeLock().lock();
        try {
            int cnt = 0;
            EntityNode node = endNode;
            for (; node != null
                    && node.commandLog.getIndex() != startIndex
                    && node.commandLog.getTerm() != term
                    ; node = node.preNode, cnt++) {
            }
            if (node != null) {
                endNode = node.preNode;
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public CommandLog[] range(final long startIndex, final long endIndex) {
        if (endIndex <= startIndex) {
            return null;
        }
        readWriteLock.readLock().lock();
        try {
            CommandLog[] entities = new CommandLog[(int) (endIndex - startIndex + 1)];
            EntityNode node = endNode;
            int index = entities.length - 1;
            for (; node != null && node.commandLog.getIndex() != startIndex
                    ; node = node.preNode, index--) {
                entities[index] = node.commandLog;
            }
            return entities;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

}
