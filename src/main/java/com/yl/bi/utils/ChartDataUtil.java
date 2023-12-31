package com.yl.bi.utils;

import cn.hutool.core.util.ObjectUtil;
import com.yl.bi.common.ErrorCode;
import com.yl.bi.exception.ThrowUtils;
import com.yl.bi.manager.AIManager;
import com.yl.bi.model.dto.chart.ChartGenResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;


@Component
@Slf4j
public class ChartDataUtil {


    /**
     * 将从数据库读出的图表信息转化为文本
     * @param chartOriginalData
     * @return
     */
    public static String changeDataToCSV(List<Map<String, Object>> chartOriginalData) {
        if(chartOriginalData==null) return null;
        List<Set<String>> columnSets = chartOriginalData.stream()
                .map(Map::keySet)
                .collect(Collectors.toList());
        List<String> columnHeader = columnSets.stream()
                .map(column -> column.stream().filter(ObjectUtil::isNotNull).collect(Collectors.joining(",")))
                .collect(Collectors.toList());
        // 拿到对应的 value 拼接上
        List<String> columnDataList = chartOriginalData.stream().map(columnData -> {
            StringBuilder result = new StringBuilder();
            String[] headers = columnHeader.get(0).split(",");
            for (int i = 0; i < headers.length; i++) {
                String data = (String) columnData.get(headers[i]);
                result.append(data);
                if (i != headers.length - 1) {
                    result.append(",");
                }
            }
            result.append("\n");
            return result.toString();
        }).collect(Collectors.toList());
        // 将 columnDataList 中的数据添加到 stringJoiner
        StringJoiner stringJoiner = new StringJoiner("");
        stringJoiner.add(columnHeader.get(0)).add("\n");
        columnDataList.forEach(stringJoiner::add);
        return stringJoiner.toString();
    }

    /**
     * 获取 AI 生成结果
     * @param aiManager  AI 能力
     * @param goal
     * @param cvsData
     * @param chartType
     * @return
     */
    public static ChartGenResult getGenResult(final AIManager aiManager, final String goal, final String cvsData, final String chartType) {
        String promote = AIManager.PRECONDITION + "分析需求 " + goal + " \n原始数据如下: " + cvsData + "\n生成图表的类型是: " + chartType;
        String resultData = aiManager.sendMesToAIUseXingHuo(promote);
        log.info("AI 生成的信息: {}", resultData);

        String[] split = resultData.split("%%%%%%");
        ThrowUtils.throwIf(split.length!=3, ErrorCode.SYSTEM_ERROR,"AI生成错误");
        String genChart = resultData.split("%%%%%%")[1].trim();
        String genResult = resultData.split("%%%%%%")[2].trim();
        return new ChartGenResult(genChart, genResult);
    }
}
