package com.hhy.circuitbreaker.proxy;

import com.hhy.circuitbreaker.IHHYCircuitBreakerController;
import org.springframework.cglib.proxy.Enhancer;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * <p>
 * 描述: 代理 Bean 工厂
 * </p>
 *
 * @Author hhy
 */
public class CglibFactory {
    public static <T> T getProxy(Object bean, Set<Integer> methods, IHHYCircuitBreakerController hhyCircuitBreakerController) throws NoSuchFieldException, IllegalAccessException {
        final Class<?> aClass = bean.getClass();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(aClass);
        enhancer.setCallback(new HHYCircuitBreakerMethodInterceptor(bean, methods, hhyCircuitBreakerController));
        final T t = (T) enhancer.create();
        // 将原始对象的字段值复制到代理对象上
        copyBeanProperties(bean, t);
        return t;
    }

    private static <T> void copyBeanProperties(Object source, T target) {
        try {
            // 获取源对象的所有字段,包括非 public 字段
            Field[] fields = source.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                // 将字段值从源对象复制到目标对象
                field.set(target, field.get(source));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("bean属性拷贝失败", e);
        }
    }
}
