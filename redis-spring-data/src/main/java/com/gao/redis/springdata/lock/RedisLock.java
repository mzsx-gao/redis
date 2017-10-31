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
            String expiresStr = String.valueOf(expires);
            if (setNX(lockKey, expiresStr)) {
                return new LockResult(lockKey, expires, true);
            }
            String currentValueStr = get(lockKey);
            if (currentValueStr != null && Long.parseLong(currentValueStr) < System.currentTimeMillis()) {
                String oldValueStr = getSet(lockKey, expiresStr);
                if (oldValueStr != null && oldValueStr.equals(currentValueStr)) {
                    return new LockResult(lockKey, expires, true);
                }
            }
            timeout -= acquiryResolutionMsecs;
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