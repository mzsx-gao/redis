package com.gao.redis.springdata.lock;

/**
 *   名称: IRedisLock.java
 *   描述: Redis实现分布式锁接口
 *   类型: JAVA
 *   最近修改时间:2017/10/30 14:15
 *   @version [版本号, V1.0]
 *   @since 2017/10/30 14:15
 *   @author gaoshudian
 */
public interface IRedisLock {

    LockResult lock(String lockKey) throws Exception;

    void unlock(LockResult lockResult);
}