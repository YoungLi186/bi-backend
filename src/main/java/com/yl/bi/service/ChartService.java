package com.yl.bi.service;

import com.yl.bi.model.dto.chart.GenChartByAiRequest;
import com.yl.bi.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yl.bi.model.entity.User;
import com.yl.bi.model.vo.BiVO;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

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
    BiVO getChart(final MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser);

    /**
     *异步的方式生成图表
     * @param multipartFile 上传的文件
     * @param genChartByAiRequest 生成图表请求
     * @param loginUser 当前登录用户
     * @return
     */
    BiVO getChartByAsync(final MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser);

}
