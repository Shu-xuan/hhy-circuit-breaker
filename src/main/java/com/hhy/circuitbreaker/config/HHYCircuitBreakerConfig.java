package com.hhy.circuitbreaker.config;

import com.hhy.circuitbreaker.HHYCircuitBreakerBeanPostProcessor;
import com.hhy.circuitbreaker.HHYCircuitBreakerController;
import com.hhy.circuitbreaker.IHHYCircuitBreakerController;
import com.hhy.circuitbreaker.alert.DefaultAlert;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * 描述: 熔断器配置
 * </p>
 *
 * @Author hhy
 */
@Configuration
public class HHYCircuitBreakerConfig {
    @Bean
    public HHYCircuitBreakerProperties properties() {
        return new HHYCircuitBreakerProperties();
    }

    @Bean("defaultAlert")
    public DefaultAlert alertService(){
        return new DefaultAlert();
    }

    @Bean
    @ConditionalOnMissingBean(HHYCircuitBreakerController.class)
    public IHHYCircuitBreakerController ihhyCircuitBreakerController(HHYCircuitBreakerProperties properties){
        return new HHYCircuitBreakerController(properties);
    }

    @Bean
    public HHYCircuitBreakerBeanPostProcessor hhyCircuitBreakerBeanPostProcessor(IHHYCircuitBreakerController ihhyCircuitBreakerController){
        return new HHYCircuitBreakerBeanPostProcessor(ihhyCircuitBreakerController);
    }
}
