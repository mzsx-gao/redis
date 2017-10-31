package com.gao.redis.springdata.lock;


/**
 *   名称: RedisLockProperties.java
 *   描述: redis锁配置
 *   类型: JAVA
 *   最近修改时间:2017/10/30 15:15
 *   @version [版本号, V1.0]
 *   @since 2017/10/30 15:15
 *   @author gaoshudian
 */
public class RedisLockProperties {

    //竞争锁间隔时间(毫秒)
    private int acquiryResolutionMsecs = 200;

    //锁占有时间(毫秒)
    private int expireMsecs = 10000;

    //获取锁等待时间(毫秒)
    private int timeoutMsecs = 30000;

    public int getAcquiryResolutionMsecs() {
        return acquiryResolutionMsecs;
    }

    public void setAcquiryResolutionMsecs(int acquiryResolutionMsecs) {
        this.acquiryResolutionMsecs = acquiryResolutionMsecs;
    }

    public int getExpireMsecs() {
        return expireMsecs;
    }

    public void setExpireMsecs(int expireMsecs) {
        this.expireMsecs = expireMsecs;
    }

    public int getTimeoutMsecs() {
        return timeoutMsecs;
    }

    public void setTimeoutMsecs(int timeoutMsecs) {
        this.timeoutMsecs = timeoutMsecs;
    }
}