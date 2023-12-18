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

     long  FIVE_MB  = 5 * 1024 * 1024L;

     List<String> validFileSuffixList  = Arrays.asList("xlsx","xls");
    BiVO getChart(final MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser);

}
