package com.minecraftforum.util;

/**
 * 雪花算法 ID 生成器
 * 用于生成唯一文件名的工具类
 */
public class SnowflakeIdGenerator {
    
    // 起始时间戳（2024-01-01 00:00:00）
    private static final long START_TIMESTAMP = 1704067200000L;
    
    // 机器ID所占的位数
    private static final long WORKER_ID_BITS = 5L;
    
    // 数据标识ID所占的位数
    private static final long DATACENTER_ID_BITS = 5L;
    
    // 支持的最大机器ID，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
    private static final long MAX_WORKER_ID = -1L ^ (-1L << WORKER_ID_BITS);
    
    // 支持的最大数据标识ID，结果是31
    private static final long MAX_DATACENTER_ID = -1L ^ (-1L << DATACENTER_ID_BITS);
    
    // 序列在ID中占的位数
    private static final long SEQUENCE_BITS = 12L;
    
    // 机器ID向左移12位
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    
    // 数据标识ID向左移17位(12+5)
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    
    // 时间戳向左移22位(5+5+12)
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    
    // 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
    private static final long SEQUENCE_MASK = -1L ^ (-1L << SEQUENCE_BITS);
    
    // 工作机器ID(0~31)
    private final long workerId;
    
    // 数据中心ID(0~31)
    private final long datacenterId;
    
    // 毫秒内序列(0~4095)
    private long sequence = 0L;
    
    // 上次生成ID的时间戳
    private long lastTimestamp = -1L;
    
    /**
     * 构造函数
     * @param workerId 工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     */
    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", MAX_DATACENTER_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }
    
    /**
     * 获得下一个ID (该方法是线程安全的)
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();
        
        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        
        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        // 时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }
        
        // 上次生成ID的时间戳
        lastTimestamp = timestamp;
        
        // 移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }
    
    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }
    
    /**
     * 返回以毫秒为单位的当前时间
     * @return 当前时间(毫秒)
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }
    
    /**
     * 获取默认的单例实例（使用默认的 workerId 和 datacenterId）
     */
    private static final SnowflakeIdGenerator INSTANCE = new SnowflakeIdGenerator(1, 1);
    
    /**
     * 获取默认实例
     * @return SnowflakeIdGenerator 实例
     */
    public static SnowflakeIdGenerator getInstance() {
        return INSTANCE;
    }
}


