package com.gao.redis.test;

import com.gao.redis.springboot.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *   名称: RedisClusterTest.java
 *   描述:
 *   类型: JAVA
 *   最近修改时间:2017/10/31 14:36
 *   @version [版本号, V1.0]
 *   @since 2017/10/31 14:36
 *   @author gaoshudian
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes= Application.class)
public class RedisClusterTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testRedisCluster(){
        redisTemplate.opsForValue().set("test","test");
    }
}