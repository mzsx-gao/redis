package cn.enjoyedu.redis.adv.rdl.demo2;

public class LockItemV2 {
    private final String key;/*锁的键值*/
    private final String value;/*锁的value*/
    private final boolean isDelay;/*锁是续期还是清除,true表示续期，false表示清除锁信息*/
    private final Thread thread;/*持有锁的线程*/

    public LockItemV2(String key, String value, boolean isDelay, Thread thread) {
        this.key = key;
        this.value = value;
        this.isDelay = isDelay;
        this.thread = thread;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public boolean isDelay() {
        return isDelay;
    }

    public Thread getThread() {
        return thread;
    }

    @Override
    public String toString() {
        return "LockItemV2{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", isDelay=" + isDelay +
                ", thread=" + thread.getName() +
                '}';
    }
}
