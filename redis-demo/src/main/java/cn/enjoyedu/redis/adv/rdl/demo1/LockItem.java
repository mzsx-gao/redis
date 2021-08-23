package cn.enjoyedu.redis.adv.rdl.demo1;


public class LockItem {
    private final String key;
    private final String value;

    public LockItem(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "LockItem{" +
            "key='" + key + '\'' +
            ", value='" + value + '\'' +
            '}';
    }
}
