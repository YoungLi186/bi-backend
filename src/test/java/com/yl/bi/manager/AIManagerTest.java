package com.yl.bi.manager;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Date: 2023/12/16 - 12 - 16 - 17:02
 * @Description: com.yl.bi.manager
 */
@SpringBootTest
@Slf4j
class AIManagerTest {


    @Resource
    private AIManager aiManager;


    @Test
    void test(){
        String hhh = aiManager.sendMesToAIUseXingHuo("hhh");
        log.info(hhh);
    }
}