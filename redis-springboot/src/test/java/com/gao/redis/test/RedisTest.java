package com.gao.redis.test;

import com.gao.redis.springboot.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes= Application.class)
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    //字符串操作
    @Test
    public void set(){
        redisTemplate.opsForValue().set("test","test");
    }

    //Hash类型操作
    @Test
    public void hsetput(){
        String key="dev:repayment:cache:contract_info";
        redisTemplate.opsForHash().put(key,"hello","hello");
    }
    @Test
    public void hsetputAll(){
        String key="dev:repayment:cache:contract_info";
        Map<String,String> map = new HashMap<>();
        map.put("hello","hello");
        map.put("hello2","hello2");
        redisTemplate.opsForHash().putAll(key,map);
    }
    @Test
    public void hget(){
        String key="dev:repayment:cache:contract_info";
        String hashKey = "hello";
        String hashValue = redisTemplate.opsForHash().get(key,hashKey).toString();
        System.out.println(hashValue);
    }
    @Test
    public void hdel(){
        String key="dev:repayment:cache:contract_info";
        String hashKey = "hello";
        redisTemplate.opsForHash().delete(key,hashKey);
    }

}