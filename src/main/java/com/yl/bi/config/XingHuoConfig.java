package com.yl.bi.config;

import io.github.briqt.spark4j.SparkClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XingHuoConfig {

    private final String appid = "9611e24f";
    private final String apiSecret = "MmZkNzU5MmQ2OWY4YmU4MzljOTM4NjY4";
    private  final String apiKey = "86ae7a4f1708e4cb5bc91d70d0094d1d";

    @Bean
    public SparkClient sparkclient() {
        SparkClient sparkClient = new SparkClient();
        sparkClient.apiKey =this.apiKey;
        sparkClient.apiSecret = this.apiSecret;
        sparkClient.appid = this.appid;
        return sparkClient;
    }
}
