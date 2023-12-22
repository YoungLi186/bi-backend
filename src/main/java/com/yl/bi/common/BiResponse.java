package com.yl.bi.common;

import com.yl.bi.common.ErrorCode;
import com.yl.bi.exception.ThrowUtils;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Getter
@NoArgsConstructor
@Data
public class BiResponse {
    private Long chartId;

    private String genChart;

    private String genResult;

    /**
     * 这里可以校验 AI 生成的内容
     */
    public BiResponse(Long chartId, String genChart, String genResult) {
        ThrowUtils.throwIf(StringUtils.isAnyBlank(genChart, genResult) || (chartId != null && chartId <= 0), ErrorCode.PARAMS_ERROR);
        this.chartId = chartId;
        this.genChart = genChart;
        this.genResult = genResult;
    }

    public BiResponse(Long chartId) {
        this.chartId = chartId;
    }
}
