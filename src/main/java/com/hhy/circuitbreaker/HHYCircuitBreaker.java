package com.hhy.circuitbreaker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 描述: 熔断注解
 * </p>
 *
 * @Author hhy
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HHYCircuitBreaker {
    /**
     * 熔断阈值：
     * 即失败比率达到这个值就熔断服务
     */
    double circuitBreakerThreshold();

    /**
     * 降级方法
     */
    String callback() default "";

    /**
     * 时间窗口大小
     */
    long timeWindowSize() default 5;

    /**
     * 时间窗口单位
     */
    TimeUnit timeunit() default TimeUnit.SECONDS;
}
