package com.hhy.circuitbreaker.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p>
 * 描述: 开启熔断服务
 * </p>
 *
 * @Author hhy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(HHYCircuitBreakerConfig.class)
public @interface EnableHHYCircuitBreaker { }
