package cn.enjoyedu.redis.adv.rdl.demo2;

import cn.enjoyedu.redis.adv.rdl.ItemVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 分布式锁，附带看门狗线程的实现V2版
 */
@Component
@Slf4j
public class RedisDistLockWithDogV2 implements Lock {

    private final static int LOCK_TIME = 1 * 1000;
    private final static String LOCK_TIME_STR = String.valueOf(LOCK_TIME);
    private final static String RS_DISTLOCK_NS = "tdln2:";

    /**
     * if redis.call('get',KEYS[1])==ARGV[1] then
     *    return redis.call('del', KEYS[1])
     * else return 0 end
     */
    private final static String RELEASE_LOCK_LUA =
        "if redis.call('get',KEYS[1])==ARGV[1] then\n" +
            "        return redis.call('del', KEYS[1])\n" +
            "    else return 0 end";


    private ThreadLocal<String> lockerId = new ThreadLocal<>();

    private Thread ownerThread;
    private String lockName = "lock";

    @Autowired
    private JedisPool jedisPool;

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public Thread getOwnerThread() {
        return ownerThread;
    }

    public void setOwnerThread(Thread ownerThread) {
        this.ownerThread = ownerThread;
    }

    @Override
    public void lock() {
        while (!tryLock()) {
            try {
                //这里也可以用wait-notify机制来实现
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean tryLock() {
        Thread t = Thread.currentThread();
        /*说明本线程正在持有锁*/
        if (ownerThread == t) {
            return true;
        } else if (ownerThread != null) {/*说明本进程中有别的线程正在持有分布式锁*/
            return false;
        }
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            /*每一个锁的持有人都分配一个唯一的id，也可采用snowflake算法*/
            String id = UUID.randomUUID().toString();

            SetParams params = new SetParams();
            params.px(LOCK_TIME);
            params.nx();
            synchronized (this) {//线程本地抢锁,这个设计很关键，先抢本地，再抢网络上
                //这个设计也很关键，类似于双重判断，如果ownerThread不为空就没必要去网络上抢锁
                if ((ownerThread == null) &&
                    "OK".equals(jedis.set(RS_DISTLOCK_NS + lockName, id, params))) {
                    lockerId.set(id);
                    setOwnerThread(t);
                    if (expireThread == null) {
                        expireThread = new Thread(new ExpireTask(), "expireThread");
                        expireThread.start();
                    }
                    delayDog.add(new ItemVo<>(LOCK_TIME, new LockItemV2(lockName, id, true, Thread.currentThread())));
                    log.info(Thread.currentThread().getName() + "已获得锁----");
                    return true;
                } else {
                    //log.info(Thread.currentThread().getName()+"无法获得锁----");
                    return false;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("分布式锁尝试加锁失败！", e);
        } finally {
            jedis.close();
        }
    }

    @Override
    public void unlock() {
        if (ownerThread != Thread.currentThread()) {
            throw new RuntimeException("试图释放无所有权的锁！");
        }
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Long result = (Long) jedis.eval(RELEASE_LOCK_LUA,
                Arrays.asList(RS_DISTLOCK_NS + lockName),
                Arrays.asList(lockerId.get()));
            if (result.longValue() != 0L) {
                log.info("Redis上的锁已释放！");
            } else {
                log.info("Redis上的锁释放失败" + ",可能是锁的key不存在，或者这个锁非本线程所有！");
            }
        } catch (Exception e) {
            delayDog.add(new ItemVo<>(RETRY_LOCK_INTERVAL,
                new LockItemV2(lockName, lockerId.get(), false, Thread.currentThread())));
            throw new RuntimeException("释放锁异常！", e);
        } finally {
            if (jedis != null) jedis.close();
            lockerId.remove();
            setOwnerThread(null);
            log.info("本地锁所有权已释放！");
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException("不支持可中断获取锁！");
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("不支持等待尝试获取锁！");
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException("不支持等待通知操作！");
    }

    /*看门狗线程*/
    private Thread expireThread;
    private static DelayQueue<ItemVo<LockItemV2>> delayDog = new DelayQueue<>();
    private final static long RETRY_LOCK_INTERVAL = 1 * 1000;
    //续期脚本
    private final static String DELAY_LOCK_LUA =
        "if redis.call('get',KEYS[1])==ARGV[1] then\n" +
            "        return redis.call('pexpire', KEYS[1],ARGV[2])\n" +
            "    else return 0 end";

    private class ExpireTask implements Runnable {

        @Override
        public void run() {
            log.info("看门狗线程已启动......");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    //log.info("开始监视锁的过期情况.....");
                    LockItemV2 lockItem = delayDog.take().getData();
                    log.info("Redis上的锁准备续期检查....." + lockItem);
                    Jedis jedis = null;
                    try {
                        jedis = jedisPool.getResource();
                        Thread lockThread = lockItem.getThread();
                        if (!lockThread.isAlive() || !lockItem.isDelay()) {
                            /*线程死亡或者清除锁操作*/
                            Long result = null;
                            try {
                                result = (Long) jedis.eval(RELEASE_LOCK_LUA,
                                    Arrays.asList(RS_DISTLOCK_NS + lockItem.getKey()),
                                    Arrays.asList(lockItem.getValue()));
                            } catch (Exception e) {
                                log.info("清除锁异常！" + e.getMessage());
                            } finally {
                                if (result == null) {
                                    delayDog.add(new ItemVo<>(RETRY_LOCK_INTERVAL,
                                        new LockItemV2(lockItem.getKey(), lockItem.getValue(), false, lockThread)));
                                    log.info("清除锁异常，快速重试！");
                                } else if (result.longValue() != 0) {
                                    log.info("Redis上的锁已清除！");
                                } else {
                                    log.info("Redis上的锁清除失败,可能是锁的key[" + RS_DISTLOCK_NS + lockItem.getKey()
                                        + "]不存在，或者这个锁非线程[" + lockThread + "][" + lockItem.getValue() + "]所有！");
                                }
                            }
                        }
                        else {/*续期操作*/
                            Long result = null;
                            try {
                                if (ownerThread == lockItem.getThread()) {/*判断本地锁所有权是否已释放，未释放需要续期*/
                                    result = (Long) jedis.eval(DELAY_LOCK_LUA,
                                        Arrays.asList(RS_DISTLOCK_NS + lockItem.getKey()),
                                        Arrays.asList(lockItem.getValue(), LOCK_TIME_STR));
                                } else {
                                    result = 0L;
                                }
                            } catch (Exception e) {
                                log.info("锁续期异常！" + e.getMessage());
                            } finally {
                                if (result == null) {
                                    delayDog.add(new ItemVo<>(RETRY_LOCK_INTERVAL,
                                        new LockItemV2(lockItem.getKey(), lockItem.getValue(), true, lockThread)));
                                    log.info("锁续期异常，快速重试！");
                                } else if (result.longValue() == 0) {
                                    log.info("锁已释放，无需续期！");
                                } else {
                                    delayDog.add(new ItemVo<>(LOCK_TIME,
                                        new LockItemV2(lockItem.getKey(), lockItem.getValue(), true, lockThread)));
                                    log.info("Redis上的锁还未释放，重新进入待续期检查！");
                                }
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("锁续期失败！", e);
                    } finally {
                        if (jedis != null) jedis.close();
                    }
                } catch (InterruptedException e) {
                    log.info("看门狗线程被中断");
                    break;
                }
            }
            log.info("看门狗线程准备关闭......");
        }
    }

//    @PostConstruct
//    public void initExpireThread(){
//
//    }

    @PreDestroy
    public void closeExpireThread() {
        if (null != expireThread) {
            expireThread.interrupt();
        }
    }
}
