package com.hhy.circuitbreaker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 描述: 方法包装类，封装后续重试所需的一些信息
 * </p>
 *
 * @Author hhy
 */
public class MethodWrapper implements Delayed {
    /**
     * 对象
     */
    private Object object;

    /**
     * 方法
     */
    private Method method;

    /**
     * 方法参数
     */
    private Object[] parameters;

    /**
     * 过期时间戳
     */
    private long expire;

    /**
     * 已重试次数
     */
    private int retriedCount;

    /**
     * 熔断裁决器
     */
    private CircuitBreakerArbiter circuitBreakerArbiter;

    public MethodWrapper(Object object, Method method, CircuitBreakerArbiter circuitBreakerArbiter) {
        this.object = object;
        this.method = method;
        this.circuitBreakerArbiter = circuitBreakerArbiter;
    }

    /**
     * 已重试次数自增，返回新值
     */
    public int incrAndGetRetryCount() {
        return ++this.retriedCount;
    }

    /**
     * 调用原方法
     */
    public boolean invoke() {
        try {
            method.invoke(object, parameters);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.expire - System.currentTimeMillis(), unit);
    }

    @Override
    public int compareTo(Delayed o) {
        return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodWrapper)) return false;
        MethodWrapper that = (MethodWrapper) o;
        return expire == that.expire && retriedCount == that.retriedCount && Objects.equals(object, that.object) && Objects.equals(method, that.method) && Arrays.equals(parameters, that.parameters) && Objects.equals(circuitBreakerArbiter, that.circuitBreakerArbiter);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(object, method, expire, retriedCount, circuitBreakerArbiter);
        result = 31 * result + Arrays.hashCode(parameters);
        return result;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public int getRetriedCount() {
        return retriedCount;
    }

    public void setRetriedCount(int retriedCount) {
        this.retriedCount = retriedCount;
    }

    public CircuitBreakerArbiter getRequestRate() {
        return circuitBreakerArbiter;
    }

    public void setRequestRate(CircuitBreakerArbiter circuitBreakerArbiter) {
        this.circuitBreakerArbiter = circuitBreakerArbiter;
    }
}
