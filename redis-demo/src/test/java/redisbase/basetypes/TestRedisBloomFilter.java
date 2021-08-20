package redisbase.basetypes;

import cn.enjoyedu.redis.redisbase.advtypes.RedisBloomFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestRedisBloomFilter {

    private static final int DAY_SEC = 60 * 60 * 24;

    @Autowired
    private RedisBloomFilter redisBloomFilter;

    @Test
    public void testInsert() throws Exception {
        System.out.println(redisBloomFilter);
        redisBloomFilter.insert("topic_read:8839540:20210810", "76930242", DAY_SEC);
        redisBloomFilter.insert("topic_read:8839540:20210810", "76930243", DAY_SEC);
        redisBloomFilter.insert("topic_read:8839540:20210810", "76930244", DAY_SEC);
        redisBloomFilter.insert("topic_read:8839540:20210810", "76930245", DAY_SEC);
        redisBloomFilter.insert("topic_read:8839540:20210810", "76930246", DAY_SEC);
    }

    @Test
    public void testMayExist() throws Exception {
        System.out.println(redisBloomFilter.mayExist("topic_read:8839540:20210810", "76930242"));
        System.out.println(redisBloomFilter.mayExist("topic_read:8839540:20210810", "76930244"));
        System.out.println(redisBloomFilter.mayExist("topic_read:8839540:20210810", "76930246"));
        System.out.println(redisBloomFilter.mayExist("topic_read:8839540:20210810", "86930250"));
    }

}
