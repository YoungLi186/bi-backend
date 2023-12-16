package com.yl.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yl.bi.common.ErrorCode;
import com.yl.bi.exception.ThrowUtils;
import com.yl.bi.manager.AIManager;
import com.yl.bi.model.dto.chart.ChartGenResult;
import com.yl.bi.model.dto.chart.GenChartByAiRequest;
import com.yl.bi.model.entity.Chart;
import com.yl.bi.model.entity.User;
import com.yl.bi.model.vo.BiVO;
import com.yl.bi.service.ChartService;
import com.yl.bi.mapper.ChartMapper;
import com.yl.bi.utils.ChartDataUtil;
import com.yl.bi.utils.ExcelUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 *
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart> implements ChartService{


    @Resource
    private AIManager aiManager;

    @Override
    public BiVO getChart(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser) {
        // 分析 xlsx 文件
        String cvsData = ExcelUtils.excelToCsv(multipartFile);

        String goal = genChartByAiRequest.getGoal();
        String name = genChartByAiRequest.getName();
        String chartType = genChartByAiRequest.getChartType();

        // 发送给 AI 分析数据
        ChartGenResult chartGenResult = ChartDataUtil.getGenResult(aiManager, goal, cvsData, chartType);
        String genChart = chartGenResult.getGenChart();
        String genResult = chartGenResult.getGenResult();

        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(cvsData);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());

        boolean saveResult = this.save(chart);
        Long charId = chart.getId();
        // 创建表、保存数据
        //saveCVSData(cvsData, charId);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "保存图表信息失败");
        return new BiVO(charId, genChart, genResult);
    }
}




