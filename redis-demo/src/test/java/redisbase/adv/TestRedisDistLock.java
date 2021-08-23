package redisbase.adv;


import cn.enjoyedu.redis.RedisBaseApplication;
import cn.enjoyedu.redis.adv.RedisDistLock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = RedisBaseApplication.class)
public class TestRedisDistLock {

    @Autowired
    private RedisDistLock redisDistLock;

    @Test
    public void testTryLock() throws Exception{
        int clientCount = 2;
        for (int i = 0; i < clientCount; i++) {
            new Thread(()->{
                redisDistLock.lock();
                System.out.println(Thread.currentThread().getName() + "已获得锁！");

                //下面两行代码时为了测试作为redis锁的key过期的问题
//                redisDistLock.setOwnerThread(null);
//                try{Thread.sleep(7000);}catch (Exception e){}
                redisDistLock.unlock();
            }).start();
        }
        Thread.sleep(Integer.MAX_VALUE);
    }

}
