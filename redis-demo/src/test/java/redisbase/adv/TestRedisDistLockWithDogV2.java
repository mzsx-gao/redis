package redisbase.adv;


import cn.enjoyedu.redis.RedisBaseApplication;
import cn.enjoyedu.redis.adv.rdl.demo2.RedisDistLockWithDogV2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest(classes = RedisBaseApplication.class)
public class TestRedisDistLockWithDogV2 {

    @Autowired
    private RedisDistLockWithDogV2 redisDistLockWithDogV2;
    private int count = 0;

    @Test
    public void testLockWithDog() throws InterruptedException {
        int clientCount = 5;
        CountDownLatch countDownLatch = new CountDownLatch(clientCount);
        ExecutorService executorService = Executors.newFixedThreadPool(clientCount);
        for (int i = 0; i < clientCount; i++) {
            executorService.execute(() -> {
                try {
                    redisDistLockWithDogV2.lock();
                    System.out.println(Thread.currentThread().getName() + "准备进行累加。");
                    Thread.sleep(2500);
                    count++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    redisDistLockWithDogV2.unlock();
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        System.out.println("最终数值:" + count);
    }
}