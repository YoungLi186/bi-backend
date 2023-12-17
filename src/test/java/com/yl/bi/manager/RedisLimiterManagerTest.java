package com.yl.bi.manager;

import com.yl.bi.config.RedissonConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Date: 2023/12/17 - 12 - 17 - 17:22
 * @Description: com.yl.bi.manager
 */
@SpringBootTest
class RedisLimiterManagerTest {


    @Resource
    private RedisLimiterManager redisLimiterManager;
    @Test
    void doRateLimit() {
        int useId = 1;
        for (int i = 0; i < 3; i++) {
            redisLimiterManager.doRateLimit(useId+"");
        }
    }
}