package com.gao.redis.springdata.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;


/**
 *   名称: RedisLockImpl.java
 *   描述: redis分布式锁实现
 *   类型: JAVA
 *   最近修改时间:2017/10/30 14:19
 *   @version [版本号, V1.0]
 *   @since 2017/10/30 14:19
 *   @author gaoshudian
 */
public class RedisLock implements IRedisLock{

    Logger log = LoggerFactory.getLogger(RedisLock.class);

    private int acquiryResolutionMsecs;
    private int expireMsecs;
    private int timeoutMsecs;

    private RedisTemplate redisTemplate;

    private static final StringRedisSerializer SERIALIZER = new StringRedisSerializer();

    public RedisLock(RedisTemplate redisTemplate, RedisLockProperties properties) {
        this.redisTemplate = redisTemplate;
        this.expireMsecs = properties.getExpireMsecs();
        this.timeoutMsecs = properties.getTimeoutMsecs();
        this.acquiryResolutionMsecs = properties.getAcquiryResolutionMsecs();
    }

    /**
     * 方法lock的功能描述：加锁
     * 实现思路: 主要是使用了redis 的setnx命令,缓存了锁.
     * reids缓存的key是锁的key,所有的共享, value是锁的到期时间(注意:这里把过期时间放在value了,没有时间上设置其超时时间)
     * 执行过程:
     * 1.通过setnx尝试设置某个key的值,成功(当前没有这个锁)则返回,成功获得锁
     * 2.锁已经存在则获取锁的到期时间,和当前时间比较,超时的话,则设置新的值
     *
     * 一些问题说明:
     * 1、为什么不直接使用expire设置超时时间，而将时间的毫秒数其作为value放在redis中？
     *   如下面的方式，把超时的交给redis处理：
     *    lock(key, expireSec){
     *    isSuccess = setnx key
     *    if (isSuccess)
     *    expire key expireSec
     *    }
     *　　这种方式貌似没什么问题，但是假如在setnx后，redis崩溃了，expire就没有执行，结果就是死锁了。锁永远不会超时。
     *   2.为什么前面的锁已经超时了，还要用getSet去设置新的时间戳的时间获取旧的值，然后和外面的判断超时时间的时间戳比较呢？
     *    String currentValueStr = get(lockKey);
     *    //判断是否为空，不为空的情况下，如果被其他线程设置了值，则第二个条件判断是过不去的
     *    if (currentValueStr != null && Long.parseLong(currentValueStr) < System.currentTimeMillis()) {//锁已经超时
     *        //获取上一个锁到期时间，并设置现在的锁到期时间，
     *        //只有一个线程才能获取上一个线上的设置时间，因为jedis.getSet是同步的
     *        String oldValueStr = getSet(lockKey, expiresStr);
     *        if (oldValueStr != null && oldValueStr.equals(currentValueStr)) {
     *            //[分布式的情况下]:如果这个时候，多个线程恰好都到了这里，但是只有一个线程的设置值和当前值相同，他才有权利获取锁
     *            return new LockResult(lockKey, expires, true);
     *        }
     *    }
     *    因为是分布式的环境下，可以在前一个锁失效的时候，有两个进程进入到锁超时的判断。如：
     *    C0超时了，还持有锁,C1/C2同时请求进入了方法里面
     *    C1/C2获取到了C0的超时时间
     *    C1使用getSet方法
     *    C2也执行了getSet方法
     *    假如我们不加 oldValueStr.equals(currentValueStr) 的判断，将会C1/C2都将获得锁，加了之后，能保证C1和C2只能一个能获得锁，
     *   一个只能继续等待。
     *
     * @author ShudianGao
     * @param [lockKey]
     * @return com.qf.auxiliaryCore.common.lock.LockResult
     * @throws
     * @since 2017/10/30 17:13
     */
    public LockResult lock(String lockKey) throws InterruptedException {
        int timeout = timeoutMsecs;
        while (timeout >= 0) {
            long expires = System.currentTimeMillis() + expireMsecs + 1;
            String expiresStr = String.valueOf(expires);//锁的到期时间
            if (setNX(lockKey, expiresStr)) {
                return new LockResult(lockKey, expires, true);
            }
            String currentValueStr = get(lockKey);
            //判断是否为空，不为空的情况下，如果被其他线程设置了值，则第二个条件判断是过不去的
            if (currentValueStr != null && Long.parseLong(currentValueStr) < System.currentTimeMillis()) {//锁已经超时
                //获取上一个锁到期时间，并设置现在的锁到期时间，
                //只有一个线程才能获取上一个线上的设置时间，因为jedis.getSet是同步的
                String oldValueStr = getSet(lockKey, expiresStr);
                if (oldValueStr != null && oldValueStr.equals(currentValueStr)) {
                    //[分布式的情况下]:如果这个时候，多个线程恰好都到了这里，但是只有一个线程的设置值和当前值相同，他才有权利获取锁
                    return new LockResult(lockKey, expires, true);
                }
            }
            timeout -= acquiryResolutionMsecs;
            //延迟100 毫秒,  这里使用随机时间可能会好一点,可以防止饥饿进程的出现,即,当同时到达多个进程,
            //只会有一个进程获得锁,其他的都用同样的频率进行尝试,后面又来了一些进程,也以同样的频率申请锁,这将可能导致前面来的锁得不到满足.
            //使用随机的等待时间可以一定程度上保证公平性
            Thread.sleep(acquiryResolutionMsecs);
        }
        return new LockResult(lockKey, 0L, false);
    }

    /**
     * 方法unlock的功能描述：解锁
     * @author ShudianGao
     * @param [lockResult]
     * @return void
     * @throws
     * @since 2017/10/30 17:14
     */
    @Override
    public void unlock(LockResult lockResult) {
        if (lockResult.isLock()) {
            long expireTime = lockResult.getExpireTime();
            if (expireTime < System.currentTimeMillis()) {

            } else {
                redisTemplate.delete(lockResult.getLockKey());
            }
        }
    }

    private String getSet(final String key, final String value) {
        Object obj = null;
        try {
            obj = redisTemplate.opsForValue().getAndSet(key, value);
        } catch (Exception e) {
            log.error("getSet redis error, key : {}", key);
        }
        return obj != null ?  obj.toString() : null;
    }

    private String get(final String key) {
        Object obj = null;
        try {
            obj = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("get redis error, key : {}", key);
        }
        return obj != null ? obj.toString() : null;
    }

    private boolean setNX(final String key, final String value) {
        Object obj = null;
        try {
            obj = this.redisTemplate.execute((RedisCallback<Boolean>) connection ->
                    connection.setNX(SERIALIZER.serialize(key), SERIALIZER.serialize(value))
            );
        } catch (Exception e) {
            log.error("setNX redis error, key : {}", key);
        }
        return obj != null ? (Boolean) obj : false;
    }
}