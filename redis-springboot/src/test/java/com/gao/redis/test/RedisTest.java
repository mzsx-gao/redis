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
    public void stringTest(){
        redisTemplate.opsForValue().set("test","test");
        System.out.println(redisTemplate.opsForValue().get("test"));

        User user = new User();
        user.setAge(29);
        user.setName("gsd");
        redisTemplate.opsForValue().set("user",user);

        System.out.println(redisTemplate.opsForValue().get("user"));

    }


    //Hash类型操作
    @Test
    public void hashTest(){
        String key="hashTest";
        redisTemplate.opsForHash().put(key,"hello","hello");

        Map<String,String> map = new HashMap<>();
        map.put("hello","hello");
        map.put("hello2","hello2");
        redisTemplate.opsForHash().putAll(key,map);

        String hashKey = "hello";
        System.out.println(redisTemplate.opsForHash().get(key,hashKey));

        redisTemplate.opsForHash().delete(key,hashKey);

    }

}