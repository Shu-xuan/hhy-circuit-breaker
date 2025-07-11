package com.hhy.circuitbreaker;

import com.hhy.circuitbreaker.proxy.CglibFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;
import java.util.HashSet;

/**
 * <p>
 * 描述:
 * </p>
 *
 * @Author hhy
 */
public class HHYCircuitBreakerBeanPostProcessor implements BeanPostProcessor {
    private IHHYCircuitBreakerController ihhyCircuitBreakerController;

    public HHYCircuitBreakerBeanPostProcessor(IHHYCircuitBreakerController ihhyCircuitBreakerController) {
        this.ihhyCircuitBreakerController = ihhyCircuitBreakerController;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        final Class<?> beanClass = bean.getClass();
        final HashSet<Integer> methodsHashCode = new HashSet<>();
        for (Method method : beanClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(HHYCircuitBreaker.class)) {
                // 熔断注解修饰的方法全部记录下它们的 hashcode
                methodsHashCode.add(method.hashCode());
            }
        }
        if (!methodsHashCode.isEmpty()) {
            try {
                return CglibFactory.getProxy(bean, methodsHashCode, ihhyCircuitBreakerController);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return bean;
    }
}
