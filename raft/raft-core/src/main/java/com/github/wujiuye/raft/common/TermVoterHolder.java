package com.github.wujiuye.raft.common;

import java.util.HashMap;
import java.util.Map;

/**
 * 每期自己拥有的选票
 *
 * @author wujiuye 2020/12/15
 */
public class TermVoterHolder {

    private final Map<Long, TermVoter> termVoterMap = new HashMap<>();
    volatile private long newTerm = 0;

    private void remove() {
        // 清空5期之前的投票状态
        long curTerm = newTerm - 5;
        for (; ; curTerm--) {
            if (termVoterMap.remove(curTerm) == null) {
                break;
            }
        }
    }

    public synchronized TermVoter getTermVoter(long term) {
        newTerm = term;
        remove();
        if (!termVoterMap.containsKey(term)) {
            termVoterMap.put(term, new TermVoter(term));
        }
        return termVoterMap.get(term);
    }

}
