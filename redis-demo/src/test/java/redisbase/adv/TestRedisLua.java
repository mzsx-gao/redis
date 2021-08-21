package redisbase.adv;


import cn.enjoyedu.redis.RedisBaseApplication;
import cn.enjoyedu.redis.adv.RedisLua;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = RedisBaseApplication.class)
public class TestRedisLua {

    @Autowired
    private RedisLua redisLua;

    @Test
    public void testLoad() {
        System.out.println(redisLua.loadScripts());
    }

    @Test
    public void tesIpLimitFlow() {
        System.out.println(redisLua.ipLimitFlow("localhost"));
    }

}
