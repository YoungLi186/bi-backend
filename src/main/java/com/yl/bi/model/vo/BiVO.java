package com.yl.bi.model.vo;

import com.yl.bi.common.ErrorCode;
import com.yl.bi.exception.ThrowUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Getter
@NoArgsConstructor
public class BiVO {
    private Long chartId;

    private String genChart;

    private String genResult;

    /**
     * 这里可以校验 AI 生成的内容
     */
    public BiVO(Long chartId, String genChart, String genResult) {
        ThrowUtils.throwIf(StringUtils.isAnyBlank(genChart, genResult) || (chartId != null && chartId <= 0), ErrorCode.PARAMS_ERROR);
        this.chartId = chartId;
        this.genChart = genChart;
        this.genResult = genResult;
    }

    public BiVO(Long chartId) {
        this.chartId = chartId;
    }
}
