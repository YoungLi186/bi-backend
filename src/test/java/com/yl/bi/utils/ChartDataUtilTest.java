package com.yl.bi.utils;

import com.yl.bi.service.ChartService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Date: 2023/12/21 - 12 - 21 - 15:50
 * @Description: com.yl.bi.utils
 */
@SpringBootTest
class ChartDataUtilTest {


    @Resource
    private ChartService chartService;
    //数据库数据转成CSV
    @Test
    void changeDataToCSV() {
        List<Map<String, Object>> maps = chartService.queryChartData(17377382974705090L);
        String s = ChartDataUtil.changeDataToCSV(maps);
        System.out.println(s);

    }

    @Test
    void getGenResult() {
    }
}