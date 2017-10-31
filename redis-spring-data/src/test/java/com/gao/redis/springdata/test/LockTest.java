package com.gao.redis.springdata.test;

import com.gao.redis.springdata.lock.IRedisLock;
import com.gao.redis.springdata.lock.LockResult;
import com.gao.redis.springdata.lock.RedisLock;
import com.gao.redis.springdata.lock.RedisLockProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *   名称: LockTest.java
 *   描述:
 *   类型: JAVA
 *   最近修改时间:2017/10/30 14:48
 *   @version [版本号, V1.0]
 *   @since 2017/10/30 14:48
 *   @author gaoshudian
 */

@ContextConfiguration(locations = {"classpath:spring-common.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class LockTest{

    @Resource
    private RedisTemplate<String, Object> redisTemplate; // 序列化操作模版

    private static final String LOCK_NO = "lock_no_";

    private static int i = 0;

    @Test
    public void testLock() throws Exception{
        ExecutorService service = Executors.newFixedThreadPool(20);
        for (int i=0;i<2;i++){
            service.execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        task("hello");
                    }catch (Exception e){

                    }
                }
            });
        }
        Thread.sleep(Integer.MAX_VALUE);
    }
    @Test
    public void test() throws Exception{
        task("hello");
    }
    private void task(String name) throws Exception{
        RedisLockProperties properties = new RedisLockProperties();
        IRedisLock redisLock = new RedisLock(redisTemplate,properties);
        LockResult result = redisLock.lock((LOCK_NO+i));
        if(result != null){
            String threadName = Thread.currentThread().getName();
            //开始执行任务
            System.out.println(threadName+"任务执行中...");
            Thread.sleep(2000);
            //任务执行完毕 关闭锁
            redisLock.unlock(result);
            System.out.println(threadName + "释放锁");
        }

    }
}