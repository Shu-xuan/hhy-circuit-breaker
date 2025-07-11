package com.hhy.circuitbreaker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * 描述: properties 或 yaml/yml 文件中的配置
 * </p>
 *
 * @Author hhy
 */
@Configuration
@ConfigurationProperties(prefix = "hhy.circuit")
public class HHYCircuitBreakerProperties {

    /**
     * 最大重试次数
     */
    @Value("${hhy.circuit.max-retry:3}")
    private int maxRetry;

    /**
     * 重试间隔
     */
    @Value("${hhy.circuit.retry-interval:5000}")
    private int retryInterval;

    /**
     * 告警器的beanName
     */
    @Value("${hhy.circuit.alert-name:defaultAlert}")
    private String alertName;

    public int getMaxRetry() {
        return maxRetry;
    }

    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    public String getAlertName() {
        return alertName;
    }

    public void setAlertName(String alertName) {
        this.alertName = alertName;
    }
}
