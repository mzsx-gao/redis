package com.gao.redis.base;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

/**
 * jedis操作redis集群
 */
public class JedsRedisClusterDemo {
    public static final int TIMEOUT = 2000; // 连接超时时间
    public static final int SO_TIMEOUT = 1000; // 间隔超时时间(即两次操作之间的间隔时间超过多少算超时)
    public static final int MAX_ATTEMPTS = 100; // 重试的次数
    public static final int MAX_TOTAL = 1000; // 设置最大连接数
    public static final int MAX_IDLE = 200; // 设置最小维持连接数
    public static final int MAX_WAIT_MILLIS = 1000; // 设置最大等待时间
    public static final boolean TEST_ON_BORROW = true; // 是否进行可用测试

    public static void main(String[] args){
        // 首先如果要想使用Jedis连接池，则必须有一个类可以保存所有连接池相关属性的配置项
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(MAX_TOTAL); // 设置最大连接数
        poolConfig.setMaxIdle(MAX_IDLE); // 设置最大空闲的连接数
        poolConfig.setMaxWaitMillis(MAX_WAIT_MILLIS);// 获取连接时的最大等待毫秒数
        poolConfig.setTestOnBorrow(TEST_ON_BORROW); // 是否要进行连接测试，以保证返回的连接为可用连接
        // 定义出所有保存RedisCluster集群主机的集合对象
        Set<HostAndPort> allRedisCluster = new HashSet<HostAndPort>();
        allRedisCluster.add(new HostAndPort("192.168.0.210", 6379));
        allRedisCluster.add(new HostAndPort("192.168.0.210", 6380));
        allRedisCluster.add(new HostAndPort("192.168.0.210", 6381));
        allRedisCluster.add(new HostAndPort("192.168.0.210", 6382));
        allRedisCluster.add(new HostAndPort("192.168.0.210", 6383));
        allRedisCluster.add(new HostAndPort("192.168.0.210", 6384));
        // 创建一个Redis集群的访问对象信息
        JedisCluster jedisCluster = new JedisCluster(allRedisCluster, TIMEOUT, SO_TIMEOUT, MAX_ATTEMPTS, poolConfig);
        for (int x = 0; x < 1000; x++) {
            jedisCluster.set("mldnjava-" + x, "Hello - " + x);
        }
        jedisCluster.close();// 关闭Redis集群连接
    }
}
