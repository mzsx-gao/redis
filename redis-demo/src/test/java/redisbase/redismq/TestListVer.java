package redisbase.redismq;

import cn.enjoyedu.redis.RedisBaseApplication;
import cn.enjoyedu.redis.redismq.ListVer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(classes = RedisBaseApplication.class)
public class TestListVer {

    @Autowired
    private ListVer listVer;

    @Test
    void testGet(){
        List<String> result = listVer.get("listmq");
        for(String message : result){
            System.out.println(message);
        }
    }

    @Test
    void testPut(){
        listVer.put("listmq","msgtest");
    }

}
