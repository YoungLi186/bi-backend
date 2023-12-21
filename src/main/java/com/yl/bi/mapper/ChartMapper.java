package com.yl.bi.mapper;

import com.yl.bi.model.entity.Chart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.yl.bi.model.entity.Chart
 */
public interface ChartMapper extends BaseMapper<Chart> {
    /**
     * 动态的创建数据表
     * @param creatTableSQL 创建表的SQL语句
     */
    void createTable(final String creatTableSQL);

    /**
     * 向动态创建的数据表之中插入数据
     * @param insertCVSData CSV文件
     * @return
     */
    void insertValue(final String insertCVSData);

    /**
     * 查询保存数据表的信息
     * @param tableName 接受图表id
     * @return
     */
    List<Map<String, Object>> queryChartData(final Long tableName);
}




