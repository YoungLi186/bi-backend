package com.yl.bi.bizmq;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.yl.bi.manager.AIManager;
import com.yl.bi.model.dto.chart.ChartGenResult;
import com.yl.bi.model.entity.Chart;
import com.yl.bi.service.ChartService;
import com.yl.bi.utils.ChartDataUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
@RocketMQMessageListener(topic = BiMqConstant.topic, selectorExpression = "*", consumerGroup = "Con_Group_One", messageModel = MessageModel.CLUSTERING)
public class MqConsumerService implements RocketMQListener<String> {
    @Resource
    private ChartService chartService;

    @Resource
    private AIManager aiManager;

    // topic需要和生产者的topic一致，consumerGroup属性是必须指定的，内容可以随意
    // selectorExpression的意思指的就是tag，默认为“*”，不设置的话会监听所有消息
    // 监听到消息就会执行此方法
    @Override
    public void onMessage(String message) {
        log.info("接收到消息 ： {}", message);
        Long chartId = Long.parseLong(message);
        if (StrUtil.isBlank(message)) {
            // 消息为空，则拒绝消息（不重试），进入死信队列
            return;
        }
        Chart chart = chartService.getById(chartId);
        if(chart ==null){
            handleChartUpdateError(chartId, "图表不存在");
            return;
        }
        String chartData = chart.getChartData();
        String goal = chart.getGoal();
        String chartType = chart.getChartType();

        //更新图标生成状态为running
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus("running");
        boolean b = chartService.updateById(updateChart);
        if (!b) {
            handleChartUpdateError(chart.getId(), "更新图标状态失败");
            return;
        }
        // 发送给 AI 分析数据
        ChartGenResult chartGenResult = ChartDataUtil.getGenResult(aiManager, goal, chartData, chartType);
        String genChart = chartGenResult.getGenChart();
        String genResult = chartGenResult.getGenResult();
        updateChart.setGenChart(genChart);
        updateChart.setGenResult(genResult);
        updateChart.setStatus("succeed");
        b = chartService.updateById(updateChart);
        if (!b) {
            handleChartUpdateError(chart.getId(), "更新图表状态失败");
        }

    }

    public void handleChartUpdateError(Long chardId, String message) {
        Chart updateChart = new Chart();
        updateChart.setId(chardId);
        updateChart.setMessage(message);
        updateChart.setStatus("failed");
        boolean b =chartService.updateById(updateChart);
        if (!b) {
            log.error("更新图表状态失败，charId:" + chardId + "," + message);
        }
    }


}
