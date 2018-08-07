package com.gao.redis.base;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


public class JedisPoolTest {

    private static Jedis jedis;
    JedisPool pool = null;

    @Before
    public void beforeTest(){

        //如果要想使用Jedis连接池,则必须有一个类可以保存所有连接池相关属性的配置项
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(1000);//设置最大连接数
        poolConfig.setMaxIdle(200);//最大空闲连接数
        poolConfig.setMaxWaitMillis(1000);//设置最大等待时间
        poolConfig.setTestOnBorrow(true);//对拿到的connection进行validateObject校验

        pool = new JedisPool(poolConfig,"172.16.36.15",6379);
        jedis = pool.getResource();
    }

    @After
    public void tearDownAfterClass() throws Exception {
        jedis.close();
        pool.close();
    }

    @Test
    public void testJedisPool(){
        System.out.println(jedis.keys("*"));
    }

}
