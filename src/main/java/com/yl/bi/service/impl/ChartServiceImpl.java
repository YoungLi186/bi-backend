package com.yl.bi.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yl.bi.bizmq.MqProducerService;
import com.yl.bi.common.ErrorCode;
import com.yl.bi.exception.BusinessException;
import com.yl.bi.exception.ThrowUtils;
import com.yl.bi.manager.AIManager;
import com.yl.bi.model.dto.chart.ChartGenResult;
import com.yl.bi.model.dto.chart.ChartQueryRequest;
import com.yl.bi.model.dto.chart.GenChartByAiRequest;
import com.yl.bi.model.entity.Chart;
import com.yl.bi.model.entity.User;
import com.yl.bi.common.BiResponse;
import com.yl.bi.model.vo.ChartVO;
import com.yl.bi.service.ChartService;
import com.yl.bi.mapper.ChartMapper;
import com.yl.bi.utils.ChartDataUtil;
import com.yl.bi.utils.ExcelUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
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

    @Resource
    private MqProducerService mqProducerService;

    @Resource
    private ChartMapper chartMapper;


//    @Resource
//    private Connection connection;

    @Override
    public BiResponse getChart(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser) {

        //拿到文件的大小和原始文件名
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();

        //检验文件大小
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1MB");

        //检验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件不合法");

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
        // chart.setChartData(cvsData);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        chart.setStatus("succeed");
        boolean saveResult = this.save(chart);
        Long charId = chart.getId();

        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "保存图表信息失败");
        //将图表元数据保存到单独的一张表
        saveCVSData(cvsData, charId);
        return new BiResponse(charId, genChart, genResult);
    }

    @Override
    public BiResponse getChartByAsync(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser) {

        Chart chart = saveChart(multipartFile, genChartByAiRequest, loginUser);
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String cvsData = chart.getChartData();
        // todo 建议处理任务队列满了后,抛异常的情况(因为提交任务报错了,前端会返回异常)
        CompletableFuture.runAsync(() -> {
            //更新图标生成状态为running
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus("running");
            boolean b = updateById(updateChart);
            if (!b) {
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
            if (!b) {
                handleChartUpdateError(chart.getId(), "更新图标状态失败");
            }
        }, threadPoolExecutor);

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return biResponse;
    }

    @Override
    public BiResponse getChartByAsyncRebuild(Long chartId) {
        Chart chart = getById(chartId);
        if(chart==null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"图表不存在");
        }
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        //分析目标为空
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标为空");
        //图标类型
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR, "图表类型不存在");
        List<Map<String, Object>> maps = queryChartData(chartId);
        String cvsData = ChartDataUtil.changeDataToCSV(maps);
        ThrowUtils.throwIf(StringUtils.isBlank(cvsData), ErrorCode.PARAMS_ERROR, "源数据不存在");
        CompletableFuture.runAsync(() -> {
            //更新图标生成状态为running
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus("running");
            boolean b = updateById(updateChart);
            if (!b) {
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
            if (!b) {
                handleChartUpdateError(chart.getId(), "更新图标状态失败");
            }
        }, threadPoolExecutor);

        return new BiResponse(chartId);
    }

    @Override
    public BiResponse getChartByMq(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser) {
        Chart chart = saveChart(multipartFile, genChartByAiRequest, loginUser);
        Long id = chart.getId();
        //将待生成的图表ID放入消息队列中
        mqProducerService.sendMsg(id.toString());
        return new BiResponse(id);
    }


    /**
     * 分页获取用户图表
     *
     * @param chartQueryRequest 图表查询请求
     * @return
     */
    @Override
    public Page<Chart> getMyChartList(ChartQueryRequest chartQueryRequest) {
        ThrowUtils.throwIf(chartQueryRequest == null, ErrorCode.PARAMS_ERROR);
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        final String chartName = chartQueryRequest.getName();
        queryWrapper.like(StringUtils.isNotBlank(chartName), "chartName", chartName);
        final String goal = chartQueryRequest.getGoal();
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        final String chartType = chartQueryRequest.getChartType();
        queryWrapper.like(StringUtils.isNotBlank(chartType), "chartType", "%" + chartType + "%");
        final Long userId = chartQueryRequest.getUserId();
        queryWrapper.eq(userId != null && userId > 0, "userId", userId);
//        final Date createTime = chartQueryRequest.getCreateTime();
//        final Date updateTime = chartQueryRequest.getUpdateTime();
//        queryWrapper.le(createTime != null, "creatTime", createTime);
//        queryWrapper.le(updateTime != null, "creatTime", createTime);
        Page<Chart> pageData = this.page(new Page<>(chartQueryRequest.getCurrent(), chartQueryRequest.getPageSize()), queryWrapper);
        ThrowUtils.throwIf(pageData == null, ErrorCode.SYSTEM_ERROR);
        //从每个具体的表中查询
        pageData.getRecords().forEach(chart -> {
            Long id = chart.getId();
            List<Map<String, Object>> chartOriginData = queryChartData(id);
            String dataToCSV = ChartDataUtil.changeDataToCSV(chartOriginData);
            chart.setChartData(dataToCSV);
        });
        return pageData;
    }


    /**
     * 处理图表更新错误
     *
     * @param chardId 图表id
     * @param message 错误信息
     */
    public void handleChartUpdateError(Long chardId, String message) {
        Chart updateChart = new Chart();
        updateChart.setId(chardId);
        updateChart.setMessage(message);
        updateChart.setStatus("failed");
        boolean b = updateById(updateChart);
        if (!b) {
            log.error("更新图表状态失败，charId:" + chardId + "," + message);
        }
    }


    /**
     * 将图表基本信息保存到数据库
     *
     * @param multipartFile       数据文件
     * @param genChartByAiRequest 生成图表请求
     * @param loginUser           当前登录用户
     * @return
     */
    public Chart saveChart(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser) {

        //拿到文件的大小和原始文件名
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        //检验文件大小
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1MB");

        //检验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件不合法");

        // 分析 xlsx 文件
        String cvsData = ExcelUtils.excelToCsv(multipartFile);
        String goal = genChartByAiRequest.getGoal();
        String name = genChartByAiRequest.getName();
        String chartType = genChartByAiRequest.getChartType();

        //将现有的信息记录到数据库中
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartType(chartType);
        chart.setUserId(loginUser.getId());
        chart.setStatus("wait");
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "保存图表信息失败");
        //将图表元数据保存到单独的一张表
        saveCVSData(cvsData, chart.getId());
        chart.setChartData(cvsData);//因为后续要继续使用源数据所以这里赋值，但数据表中并没有存入源数据
        return chart;
    }


    /**
     * 将每个图表原始数据单独放入一个表中
     * 生成建表格 SQL 并且插入 cvs 数据到表中
     *
     * @param cvsData
     * @param chartId
     */
    private void saveCVSData(final String cvsData, final Long chartId) {
        String[] columnHeaders = cvsData.split("\n")[0].split(",");
        StringBuilder sqlColumns = new StringBuilder();
        for (int i = 0; i < columnHeaders.length; i++) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(columnHeaders[i]), ErrorCode.PARAMS_ERROR);
            sqlColumns.append("`").append(columnHeaders[i]).append("`").append(" varchar(50) NOT NULL");
            if (i != columnHeaders.length - 1) {
                sqlColumns.append(", ");
            }
        }
        String sql = String.format("CREATE TABLE charts_%d ( %s )", chartId, sqlColumns);
        String[] columns = cvsData.split("\n");
        StringBuilder insertSql = new StringBuilder();
        insertSql.append("INSERT INTO charts_").append(chartId).append(" VALUES ");
        for (int i = 1; i < columns.length; i++) {
            String[] strings = columns[i].split(",");
            insertSql.append("(");
            for (int j = 0; j < strings.length; j++) {
                insertSql.append("'").append(strings[j]).append("'");
                if (j != strings.length - 1) {
                    insertSql.append(", ");
                }
            }
            insertSql.append(")");
            if (i != columns.length - 1) {
                insertSql.append(", ");
            }
        }
        try {
            chartMapper.createTable(sql);
            chartMapper.insertValue(insertSql.toString());
        } catch (Exception e) {
            log.error("插入数据报错 " + e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 查询保存到数据库之中的 cvs 数据
     *
     * @param chartId 图表编号
     * @return
     */
    public List<Map<String, Object>> queryChartData(final Long chartId) {
        try {
            return chartMapper.queryChartData(chartId);
        } catch (BadSqlGrammarException e) {
            return null;
        }
    }


    /**
     * 获取图表的封装类
     * @param chart
     * @return
     */
    @Override
    public ChartVO getChartVO(Chart chart) {
        if(chart==null){
            return null;
        }
        ChartVO chartVO = new ChartVO();
        BeanUtils.copyProperties(chart,chartVO);
        return  chartVO;
    }

    @Override
    public Chart getChartById(Long id) {
        if(id==null){
            return null;
        }
        Chart chart = getById(id);
        if(chart==null){
            return null;
        }
        List<Map<String, Object>> maps = queryChartData(id);
        String chartData = ChartDataUtil.changeDataToCSV(maps);
        chart.setChartData(chartData);
        return chart;
    }


}




