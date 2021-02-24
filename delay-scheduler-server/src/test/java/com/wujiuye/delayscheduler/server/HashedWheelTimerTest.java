package com.wujiuye.delayscheduler.server;

import com.wujiuye.delayscheduler.server.hashedwheeltimer.HashedWheelCallback;
import com.wujiuye.delayscheduler.server.hashedwheeltimer.HashedWheelTimer;
import com.wujiuye.delayscheduler.server.hashedwheeltimer.TimerTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HashedWheelTimerTest {

    private HashedWheelTimer hashedWheelTimer;
    private HashedWheelCallback hashedWheelCallback;

    @Before
    public void before() {
        this.hashedWheelCallback = (rounds, tasks) -> {
            for (TimerTask timerTask : tasks) {
                System.out.println("run task " + timerTask.getTaskId() + " by rounds " + rounds);
            }
        };
        this.hashedWheelTimer = new HashedWheelTimer(hashedWheelCallback);
    }

    @Test
    public void testHashedWheelTimer() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            int min = 1;
            int max = 10;
            int delay = (int) (Math.random() * (max - min + 1));
            hashedWheelTimer.putTask(i + "", System.currentTimeMillis() / 1000, 2);
            if (delay > 5) {
                System.out.println(hashedWheelTimer);
                TimeUnit.MILLISECONDS.sleep(1000);
            }
        }
    }

    @After
    public void after() throws IOException, InterruptedException {
        TimeUnit.MILLISECONDS.sleep(3000);
        hashedWheelTimer.close();
    }

}
