package com.hhy.circuitbreaker.proxy;

import com.hhy.circuitbreaker.CircuitBreakerArbiter;
import com.hhy.circuitbreaker.HHYCircuitBreaker;
import com.hhy.circuitbreaker.IHHYCircuitBreakerController;
import com.hhy.circuitbreaker.MethodWrapper;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 描述: 熔断对象方法拦截器
 * </p>
 *
 * @Author hhy
 */
public class HHYCircuitBreakerMethodInterceptor implements MethodInterceptor {
    private Object object;
    private Set<Integer> methodHashCode;
    private Map<Integer, CircuitBreakerArbiter> arbiterMap;
    private IHHYCircuitBreakerController hhyCircuitBreakerController;
    /**
     * 请求时间窗口
     */
    private long windowRequestTime = Long.MIN_VALUE;

    public HHYCircuitBreakerMethodInterceptor(Object object, Set<Integer> methodHashCode, IHHYCircuitBreakerController hhyCircuitBreakerController) {
        this.object = object;
        this.methodHashCode = methodHashCode;
        this.arbiterMap = new HashMap<>();
        this.hhyCircuitBreakerController = hhyCircuitBreakerController;
    }

    /**
     * 所有生成的代理方法都会调用这个方法而不是原始方法
     * 原始方法也可能被一般的反射使用方法对象来调用，或者通过方法代理（更快）
     * @param o "this", 被增强的对象
     * @param method 被拦截的方法
     * @param objects 参数数组; primitive types are wrapped
     * @param methodProxy 用来调用原始类中没有被代理的方法; 根据需要可能被调用多次
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        final int hashCode = method.hashCode();
        if (!methodHashCode.contains(hashCode)) {
            return methodProxy.invokeSuper(o, objects);
        }
        HHYCircuitBreaker hhyCircuitBreaker = method.getAnnotation(HHYCircuitBreaker.class);

        if (hhyCircuitBreakerController.exist(hashCode)) {
            final String callbackName = hhyCircuitBreaker.callback();
            // 返回降级方法的调用结果
            return this.object.getClass().getMethod(callbackName).invoke(object);
        }
        // 记录
        if (!arbiterMap.containsKey(hashCode)) {
            // 每个方法一个熔断裁决器
            arbiterMap.put(hashCode, new CircuitBreakerArbiter());
        }

        boolean result = true;
        // 执行结果
        Object returnVal = null;
        try {
            returnVal = methodProxy.invokeSuper(o, objects);
        } catch (Throwable e) {
            result = false;
        } finally {
            CircuitBreakerArbiter arbiter = arbiterMap.get(hashCode);

            final long windowSize = hhyCircuitBreaker.timeWindowSize();
            TimeUnit timeunit = hhyCircuitBreaker.timeunit();
            final long currentTime = System.currentTimeMillis();
            if (currentTime > windowRequestTime){
                // 窗口已过期，重置统计并更新窗口时间
                windowRequestTime = timeunit.toMillis(windowSize) + currentTime;
                arbiter.reset(); // 清空计数器
            }
            if (result) {
                arbiter.incrSuccess();
            } else {
                arbiter.incrFail();
            }
            // 失败率达到阈值
            if (!arbiter.isFailureRateTolerable(hhyCircuitBreaker.circuitBreakerThreshold())) {
                // 注意传入原始对象，而不是被其他注解增强过的代理对象
                // 如果此时仍然通过代理执行方法，就会：
                // 再次进入拦截器逻辑；
                // 可能再次触发熔断判断；
                // 导致重复统计、无限循环、甚至堆栈溢出等问题
                hhyCircuitBreakerController.trigger(new MethodWrapper(object, method, arbiter));
            }
        }
        return returnVal;
    }
}
