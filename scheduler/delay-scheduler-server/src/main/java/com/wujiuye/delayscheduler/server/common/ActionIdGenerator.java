package com.wujiuye.delayscheduler.server.common;

/**
 * 任务ID生成器
 *
 * @author wujiuye 2020/08/24
 */
public class ActionIdGenerator {

    private static final SnowFlake SNOW_FLAKE = new SnowFlake(1,
            Math.abs(IpAndProcessIdUtils.getLocalAddress().hashCode()) % 31);

    /**
     * 使用雪花算法生成唯一ID
     *
     * @return
     */
    public static Long generator() {
        return SNOW_FLAKE.nextId();
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            System.out.println(ActionIdGenerator.generator());
        }
    }

    private static class SnowFlake {
        /**
         * 起始的时间戳:这个时间戳自己随意获取，比如自己代码的时间戳
         */
        private final static long START_STMP = 1598326404076L;
        /**
         * 每一部分占用的位数
         */
        private final static long SEQUENCE_BIT = 12; //序列号占用的位数
        private final static long MACHINE_BIT = 5;  //机器标识占用的位数
        private final static long DATACENTER_BIT = 5;//数据中心占用的位数

        /**
         * 每一部分的最大值：先进行左移运算，再同-1进行异或运算；异或：相同位置相同结果为0，不同结果为1
         */
        /**
         * 用位运算计算出最大支持的数据中心数量：31
         */
        private final static long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);

        /**
         * 用位运算计算出最大支持的机器数量：31
         */
        private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);

        /**
         * 用位运算计算出12位能存储的最大正整数：4095
         */
        private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

        /**
         * 机器标志较序列号的偏移量
         */
        private final static long MACHINE_LEFT = SEQUENCE_BIT;

        /**
         * 数据中心较机器标志的偏移量
         */
        private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;

        /**
         * 时间戳较数据中心的偏移量
         */
        private final static long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

        private long datacenterId;   // 数据中心
        private long machineId;      // 机器标识
        private long sequence = 0L;  // 序列号
        private long lastStmp = -1L; // 上一次时间戳

        private SnowFlake(long datacenterId, long machineId) {
            this.datacenterId = datacenterId;
            this.machineId = machineId;
        }

        /**
         * 产生下一个ID
         *
         * @return
         */
        public synchronized long nextId() {
            // 获取当前时间戳
            long currStmp = getNewstmp();

            // 如果当前时间戳小于上次时间戳则抛出异常
            if (currStmp < lastStmp) {
                throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
            }
            // 相同毫秒内
            if (currStmp == lastStmp) {
                // 相同毫秒内，序列号自增
                sequence = (sequence + 1) & MAX_SEQUENCE;
                // 同一毫秒的序列数已经达到最大
                if (sequence == 0L) {
                    // 获取下一时间的时间戳并赋值给当前时间戳
                    currStmp = getNextMill();
                }
            } else {
                // 不同毫秒内，序列号置为0
                sequence = 0L;
            }
            // 当前时间戳存档记录，用于下次产生id时对比是否为相同时间戳
            lastStmp = currStmp;

            return (currStmp - START_STMP) << TIMESTMP_LEFT //时间戳部分
                    | datacenterId << DATACENTER_LEFT      //数据中心部分
                    | machineId << MACHINE_LEFT            //机器标识部分
                    | sequence;                            //序列号部分
        }

        private long getNextMill() {
            long mill = getNewstmp();
            while (mill <= lastStmp) {
                mill = getNewstmp();
            }
            return mill;
        }

        private static long getNewstmp() {
            return System.currentTimeMillis();
        }

    }

}
