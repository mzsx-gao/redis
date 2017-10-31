package com.gao.redis.springdata.lock;

/**
 *   名称: LockResult.java
 *   描述: redis锁返回结果
 *   类型: JAVA
 *   最近修改时间:2017/10/30 15:08
 *   @version [版本号, V1.0]
 *   @since 2017/10/30 15:08
 *   @author gaoshudian
 */
public class LockResult {

    //锁的key
    private String lockKey;

    //锁占有时间(毫秒) 默认10秒
    private long expireTime;

    //是否已锁定
    private boolean isLock = false;


    public LockResult(){

    }

    public LockResult(String lockKey, long expireTime, boolean isLock) {
        this.lockKey = lockKey;
        this.expireTime = expireTime;
        this.isLock = isLock;
    }

    public String getLockKey() {
        return lockKey;
    }

    public void setLockKey(String lockKey) {
        this.lockKey = lockKey;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public boolean isLock() {
        return isLock;
    }

    public void setLock(boolean lock) {
        isLock = lock;
    }
}