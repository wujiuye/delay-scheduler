package com.wujiuye.delayscheduler.client;

import com.wujiuye.delayscheduler.core.ActionBindManager;
import com.wujiuye.delayscheduler.core.ActionSupper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DelaySchedulerTest {

    public final static class MyAction extends ActionSupper<String> {

        private String taskName;

        public MyAction() {

        }

        public MyAction(String taskName) {
            this.taskName = taskName;
        }

        @Override
        public String getTaskName() {
            return this.taskName;
        }

        @Override
        public String getParam() {
            return this.taskName;
        }

        @Override
        public boolean execute(int curPeriod, String param) {
            System.out.println("[" + curPeriod + "] ==> " + param + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            return curPeriod >= 5;
        }

    }

    public static void main(String[] args) throws Exception {
        DelayScheduler.startDelaySchedulerClient("test",
                "127.0.0.1:8080,127.0.0.1:8081,127.0.0.1:8082",
                1000, 1500);
        for (int i = 0; i < 26; i++) {
            String actionName = "testAction-" + ((char) ('a' + i));
            ActionBindManager.bindAction("test", actionName, MyAction.class);
            try {
                long id = DelayScheduler.getScheduleService().submitAction(new MyAction(actionName), TimeUnit.SECONDS, 2, 4, 8, 12, 16);
                System.out.println("action " + actionName + " id is " + id);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            Thread.sleep(8000 + new Random().nextInt(100));
        }
        System.in.read();
    }

}
