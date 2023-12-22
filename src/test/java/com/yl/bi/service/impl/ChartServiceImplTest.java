package com.yl.bi.service.impl;

import com.yl.bi.model.entity.Chart;
import com.yl.bi.service.ChartService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Date: 2023/12/22 - 12 - 22 - 16:43
 * @Description: com.yl.bi.service.impl
 */
@SpringBootTest
class ChartServiceImplTest {

    @Resource
    private ChartService chartService;

    @Test
    void saveChartToRedis() {



    }


    @Test
    void testSaveChartToRedis() {
        Chart chart = new Chart();
        chart.setId(8371497294712L);
        chart.setGoal("fasfa");
        chart.setName("sadas");
        chart.setChartData("dasda");
        chart.setChartType("dasaa");
        chart.setGenChart("dasad");
        chart.setGenResult("sadas");
        chart.setUserId(321839012831L);
        chart.setStatus("success");
        chartService.saveChartToRedis(chart);
    }


    @Test
    void testDeleteChartInRedis(){
        chartService.deleteChartInRedis(8371497294712L);
    }
}