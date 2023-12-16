package com.yl.bi.service;

import com.yl.bi.model.dto.chart.GenChartByAiRequest;
import com.yl.bi.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yl.bi.model.entity.User;
import com.yl.bi.model.vo.BiVO;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 */
public interface ChartService extends IService<Chart> {

    BiVO getChart(final MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser);

}
