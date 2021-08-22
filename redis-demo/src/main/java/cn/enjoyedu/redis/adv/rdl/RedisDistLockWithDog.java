package cn.enjoyedu.redis.adv.rdl;

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
 * 分布式锁，附带看门狗线程的实现
 */
@Component
public class RedisDistLockWithDog implements Lock {

    private final static int LOCK_TIME = 1 * 1000;
    private final static String LOCK_TIME_STR = String.valueOf(LOCK_TIME);
    private final static String RS_DISTLOCK_NS = "tdln2:";
    /*
     if redis.call('get',KEYS[1])==ARGV[1] then
        return redis.call('del', KEYS[1])
    else return 0 end
     */
    private final static String RELEASE_LOCK_LUA =
            "if redis.call('get',KEYS[1])==ARGV[1] then\n" +
                    "        return redis.call('del', KEYS[1])\n" +
                    "    else return 0 end";

    private ThreadLocal<String> lockerId = new ThreadLocal<>();

    private Thread ownerThread;
    private String lockName = "lock";/*还有并发问题，考虑ThreadLocal*/

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
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException("不支持可中断获取锁！");
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
            synchronized (this) {
                if ((ownerThread == null) &&
                        "OK".equals(jedis.set(RS_DISTLOCK_NS + lockName, id, params))) {
                    lockerId.set(id);
                    setOwnerThread(t);
//                    System.out.println(RS_DISTLOCK_NS+lockName);
//                    System.out.println(lockerId.get());
                    if (expireThread == null) {
                        expireThread = new Thread(new ExpireTask(), "expireThread");
                        expireThread.start();
                    }
                    delayDog.add(new ItemVo<>((int) LOCK_TIME, new LockItem(lockName, id)));
                    System.out.println(Thread.currentThread().getName() + "已获得锁----");
                    return true;
                } else {
                    System.out.println(Thread.currentThread().getName() + "无法获得锁----");
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
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("不支持等待尝试获取锁！");
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
            System.out.println(result);
//            System.out.println(RS_DISTLOCK_NS+lockName);
//            System.out.println(lockerId.get());
            if (result.longValue() != 0L) {
                System.out.println("Redis上的锁已释放！");
            } else {
                System.out.println("Redis上的锁释放失败！");
            }
        } catch (Exception e) {
            throw new RuntimeException("释放锁失败！", e);
        } finally {
            if (jedis != null) jedis.close();
            lockerId.remove();
            setOwnerThread(null);
            System.out.println("本地锁所有权已释放！");
        }
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException("不支持等待通知操作！");
    }

    /*看门狗线程*/
    private Thread expireThread;
    private static DelayQueue<ItemVo<LockItem>> delayDog
            = new DelayQueue<>();
    private final static String DELAY_LOCK_LUA =
            "if redis.call('get',KEYS[1])==ARGV[1] then\n" +
                    "        return redis.call('pexpire', KEYS[1],ARGV[2])\n" +
                    "    else return 0 end";

    private class ExpireTask implements Runnable {

        @Override
        public void run() {
            System.out.println("看门狗线程已启动......");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    System.out.println("开始监视锁的过期情况.....");
                    LockItem lockItem = delayDog.take().getData();
                    System.out.println("Redis上的锁准备续期....." + lockItem);
                    Jedis jedis = null;
                    try {
                        jedis = jedisPool.getResource();
                        Long result = (Long) jedis.eval(DELAY_LOCK_LUA,
                                Arrays.asList(RS_DISTLOCK_NS + lockItem.getKey()),
                                Arrays.asList(lockItem.getValue(), LOCK_TIME_STR));
//                        System.out.println(RS_DISTLOCK_NS+lockItem.getKey());
//                        System.out.println(lockItem.getValue());
                        if (result.longValue() == 0L) {
                            System.out.println("Redis上的锁已释放，无需续期！");
                        } else {
                            /*可能出现为无限续期的情况，解决方案可以有
                             * 1、最大续期次数
                             * 2、判断工作的线程存活情况
                             * 3、由工作线程通知看门狗来续期*/
                            delayDog.add(new ItemVo<>((int) LOCK_TIME,
                                    new LockItem(lockItem.getKey(), lockItem.getValue())));
                            System.out.println("Redis上的锁还未释放，重新进入待续期检查！");
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("锁续期失败！", e);
                    } finally {
                        if (jedis != null) jedis.close();
                    }
                } catch (InterruptedException e) {
                    System.out.println("看门狗线程被中断");
                    break;
                }
            }
            System.out.println("看门狗线程准备关闭......");
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
