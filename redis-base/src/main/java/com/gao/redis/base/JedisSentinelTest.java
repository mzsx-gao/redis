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

    public static final String MASTER_NAME = "mymaster" ;	// 定义哨兵的Master配置名称
    public static final int TIMEOUT = 2000 ;	// 连接超时时间
    public static final String REDIS_AUTH = "mldnjava" ;	// 认证密码
    public static final int MAX_TOTAL = 1000 ;	// 设置最大连接数
    public static final int MAX_IDLE = 200 ;	// 设置最小维持连接数
    public static final int MAX_WAIT_MILLIS = 1000 ;	// 设置最大等待时间

    public static void main(String[] args) {
        // 如果要通过哨兵机制进行Redis访问，那么必须要明确的设置出所有可以使用的哨兵的地址与端口
        Set<String> sentinels = new HashSet<String>() ;	// 设置所有的哨兵的处理地址信息
        sentinels.add("192.168.0.103:26379") ;	// 哨兵的地址
        sentinels.add("192.168.0.103:26380") ;	// 哨兵的地址
        sentinels.add("192.168.0.103:26381") ;	// 哨兵的地址
        // 首先如果要想使用Jedis连接池，则必须有一个类可以保存所有连接池相关属性的配置项
        JedisPoolConfig poolConfig = new JedisPoolConfig() ;
        poolConfig.setMaxTotal(MAX_TOTAL); 	// 设置最大连接数
        poolConfig.setMaxIdle(MAX_IDLE); 	// 设置空闲的连接数
        poolConfig.setMaxWaitMillis(MAX_WAIT_MILLIS);// 最大等待时间
        // 此时所有的连接应该通过哨兵机制取得，所以这个时候应该使用JedisSentinelPool对象
        JedisSentinelPool pool = new JedisSentinelPool(MASTER_NAME, sentinels,poolConfig);	// 建立一个哨兵的连接池
        Jedis jedis = pool.getResource() ;	// 通过连接池获取连接对象
//        jedis.auth(REDIS_AUTH) ;
        System.out.println(jedis);
        jedis.set("mldn", "www.mldn.cn") ;
        jedis.get("mldn");
        jedis.close();
        pool.close(); 	// 关闭连接池
    }
}
