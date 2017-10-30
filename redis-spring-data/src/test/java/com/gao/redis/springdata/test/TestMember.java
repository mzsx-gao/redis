package com.gao.redis.springdata.test;

import java.util.Date;
import javax.annotation.Resource;
import com.gao.redis.springdata.Member;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = {"classpath:spring-common.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class TestMember {
    @Resource
    private RedisTemplate<String, Object> redisTemplate; // 序列化操作模版

    @Test
    public void testLoad() {
        Object obj = this.redisTemplate.opsForValue().get("mldn-1"); // 要进行转型处理
        System.out.println(obj);
    }
    @Test
    public void testSave() { // 数据保存处理
        Member vo = new Member();
        vo.setMid("mldn-java");
        vo.setBirthday(new Date());
        vo.setName("张三");
        vo.setAge(18);
        vo.setSalary(1.1);
        this.redisTemplate.opsForValue().set("mldn-1", vo);
    }
    @Test
    public void testMultiSave() throws Exception { // 数据保存处理
        for (int x = 0; x < 10000; x++) {
            Member vo = new Member();
            vo.setMid("mldn-java");
            vo.setBirthday(new Date());
            vo.setName("张三");
            vo.setAge(18);
            vo.setSalary(1.1);
            this.redisTemplate.opsForValue().set("mldn-" + x, vo);
        }
    }
}
