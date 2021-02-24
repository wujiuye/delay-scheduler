package com.github.wujiuye.raft;

import org.junit.Test;

public class RaftConfigTest {

    @Test
    public void testConfig() {
        System.out.println(Raft.getRaftConfig().getElectionMs());
    }

}
