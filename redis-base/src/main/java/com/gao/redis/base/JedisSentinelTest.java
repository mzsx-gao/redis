package com.gao.redis.base;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import java.util.HashSet;
import java.util.Set;

/**
 * jedis操作redis哨兵模式
 */
public class JedisSentinelTest {

    public static final int MAX_TOTAL = 1000;    // 设置最大连接数
    public static final int MAX_IDLE = 200;    // 设置最小维持连接数
    public static final int MAX_WAIT_MILLIS = 1000;    // 设置最大等待时间

    public static final String MASTER_NAME = "mymaster";    // 定义哨兵的Master配置名称

    public static void main(String[] args) {
        // 如果要通过哨兵机制进行Redis访问，那么必须要明确的设置出所有可以使用的哨兵的地址与端口
        Set<String> sentinels = new HashSet<>();
        sentinels.add("172.16.216.128:26380");
        sentinels.add("172.16.216.128:26381");
        sentinels.add("172.16.216.128:26382");

        // Jedis连接池配置
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(MAX_TOTAL);
        poolConfig.setMaxIdle(MAX_IDLE);
        poolConfig.setMaxWaitMillis(MAX_WAIT_MILLIS);

        // 此时所有的连接应该通过哨兵机制取得，所以这个时候应该使用JedisSentinelPool对象
        JedisSentinelPool pool = new JedisSentinelPool(MASTER_NAME, sentinels, poolConfig);
        Jedis jedis = pool.getResource();    // 通过连接池获取连接对象

        System.out.println("获取到redis连接地址:" +
                jedis.getClient().getSocket().getInetAddress() + ":" + jedis.getClient().getSocket().getPort());

        jedis.set("testKey", "testValue");
        jedis.get("testKey");
        jedis.close();
        pool.close();
    }
}
