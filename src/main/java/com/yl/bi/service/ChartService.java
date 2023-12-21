package com.yl.bi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yl.bi.model.dto.chart.ChartQueryRequest;
import com.yl.bi.model.dto.chart.GenChartByAiRequest;
import com.yl.bi.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yl.bi.model.entity.User;
import com.yl.bi.model.vo.BiVO;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 */
public interface ChartService extends IService<Chart> {

     long  ONE_MB  = 1024 * 1024L;

     List<String> validFileSuffixList  = Arrays.asList("xlsx","xls");

    /**
     *以同步的方式生成图表
     * @param multipartFile 上传的文件
     * @param genChartByAiRequest 生成图表请求
     * @param loginUser 当前登录用户
     * @return
     */
    BiVO  getChart(final MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser);

    /**
     *异步的方式生成图表
     * @param multipartFile 上传的文件
     * @param genChartByAiRequest 生成图表请求
     * @param loginUser 当前登录用户
     * @return
     */
    BiVO getChartByAsync(final MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser);


    /**
     *基于消息队列实现异步生成图表
     * @param multipartFile 上传的文件
     * @param genChartByAiRequest 生成图表请求
     * @param loginUser 当前登录用户
     * @return
     */
    BiVO getChartByMq(final MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser);




    Page<Chart> getMyChartList(ChartQueryRequest chartQueryRequest);




    /**
     * 查询保存到数据库之中的 cvs 数据
     * @param chartId 图表编号
     * @return
     */
     List<Map<String, Object>> queryChartData(final Long chartId) throws BadSqlGrammarException;

}
