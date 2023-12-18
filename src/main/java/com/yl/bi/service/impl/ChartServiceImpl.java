package com.yl.bi.service.impl;

import cn.hutool.core.io.FileUtil;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart> implements ChartService {


    @Resource
    private AIManager aiManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public BiVO getChart(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser) {

        //拿到文件的大小和原始文件名
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();

        //检验文件大小
        ThrowUtils.throwIf(size>ONE_MB,ErrorCode.PARAMS_ERROR,"文件超过 1MB");

        //检验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix),ErrorCode.PARAMS_ERROR,"文件不合法");

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

        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "保存图表信息失败");
        return new BiVO(charId, genChart, genResult);
    }

    @Override
    public BiVO getChartByAsync(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser) {

        //拿到文件的大小和原始文件名
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        //检验文件大小
        ThrowUtils.throwIf(size>ONE_MB,ErrorCode.PARAMS_ERROR,"文件超过 1MB");

        //检验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix),ErrorCode.PARAMS_ERROR,"文件不合法");

        // 分析 xlsx 文件
        String cvsData = ExcelUtils.excelToCsv(multipartFile);
        String goal = genChartByAiRequest.getGoal();
        String name = genChartByAiRequest.getName();
        String chartType = genChartByAiRequest.getChartType();


        //将现有的信息记录到数据库中
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(cvsData);
        chart.setChartType(chartType);
        chart.setUserId(loginUser.getId());
        chart.setStatus("wait");
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "保存图表信息失败");


        // todo 建议处理任务队列满了后,抛异常的情况(因为提交任务报错了,前端会返回异常)
        CompletableFuture.runAsync(()->{
            //更新图标生成状态为running
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus("running");
            boolean b = updateById(updateChart);
            if(!b){
                handleChartUpdateError(chart.getId(), "更新图标状态失败");
            }
            // 发送给 AI 分析数据
            ChartGenResult chartGenResult = ChartDataUtil.getGenResult(aiManager, goal, cvsData, chartType);
            String genChart = chartGenResult.getGenChart();
            String genResult = chartGenResult.getGenResult();
            updateChart.setGenChart(genChart);
            updateChart.setGenResult(genResult);
            updateChart.setStatus("succeed");
             b = updateById(updateChart);
            if(!b){
                handleChartUpdateError(chart.getId(), "更新图标状态失败");
            }
        },threadPoolExecutor);


        BiVO biVO = new BiVO();
        biVO.setChartId(chart.getId());
        return biVO;
    }


    public void handleChartUpdateError(Long chardId,String message){
        Chart updateChart = new Chart();
        updateChart.setId(chardId);
        updateChart.setMessage(message);
        updateChart.setStatus("failed");
        boolean b = updateById(updateChart);
        if(!b){
            log.error("更新图表状态失败，charId:"+chardId+","+message);
        }
    }

}




