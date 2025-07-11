package com.hhy.circuitbreaker;

/**
 * <p>
 * 描述: 熔断控制器
 * </p>
 *
 * @Author hhy
 */
public interface IHHYCircuitBreakerController {

    /**
     * 触发熔断
     * @param methodInvoker
     */
    void trigger(MethodWrapper methodInvoker);

    /**
     * 被熔断方法是否已包含当前方法
     * @param methodHashCode 方法哈希值
     * @return
     */
    boolean exist(int methodHashCode);
}
