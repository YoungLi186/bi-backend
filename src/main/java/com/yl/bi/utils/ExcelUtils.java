package com.yl.bi.utils;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Date: 2023/12/15 - 12 - 15 - 21:34
 * @Description: com.yl.bi.utils
 * 将Excel文件转换为CSV
 */
@Slf4j
public class ExcelUtils {

    /**
     * 将用户上传的Excel文件转换为CSV格式
     * @param multipartFile
     * @return
     */
    public static String excelToCsv(MultipartFile multipartFile) {
            // 读取数据
            List<Map<Integer, String>> list = null;
            try {
                list = EasyExcel.read(multipartFile.getInputStream())
                        .excelType(ExcelTypeEnum.XLSX)
                        .sheet()
                        .headRowNumber(0)
                        .doReadSync();
            } catch (IOException e) {
                log.error("excel处理错误");
            }
            // 如果数据为空
            if (CollUtil.isEmpty(list)) {
                return "";
            }
            // 转换为 csv
            StringBuilder stringBuilder = new StringBuilder();
            // 读取表头(第一行)
            LinkedHashMap<Integer, String> headerMap = (LinkedHashMap) list.get(0);
            List<String> headerList = headerMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            stringBuilder.append(StringUtils.join(headerList,",")).append("\n");
            // 读取数据(读取完表头之后，从第一行开始读取)
            for (int i = 1; i < list.size(); i++) {
                LinkedHashMap<Integer, String> dataMap = (LinkedHashMap) list.get(i);
                List<String> dataList = dataMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
                stringBuilder.append(StringUtils.join(dataList,",")).append("\n");
            }
            return stringBuilder.toString();
        }
}
