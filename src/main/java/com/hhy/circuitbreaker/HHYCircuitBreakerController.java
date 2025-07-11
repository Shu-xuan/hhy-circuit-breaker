package com.hhy.circuitbreaker;

import com.hhy.circuitbreaker.alert.IAlertService;
import com.hhy.circuitbreaker.config.HHYCircuitBreakerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 描述: 熔断控制器实现
 * </p>
 *
 * @Author hhy
 */
@Component
public class HHYCircuitBreakerController implements IHHYCircuitBreakerController, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(HHYCircuitBreakerController.class);

    private ApplicationContext applicationContext;

    /**
     * 线程池
     */
    private static ThreadPoolTaskExecutor executor;

    static {
        executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(2);
        // 最大线程数
        executor.setMaxPoolSize(4);
        // 队列容量
        executor.setQueueCapacity(96);
        // 存活时长：60秒
        executor.setKeepAliveSeconds(60);
        // 线程名称前缀
        executor.setThreadNamePrefix("circuit-breaker-");
        // 拒绝策略：抛异常
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        // 设置线程工厂
        executor.setThreadFactory(Thread::new);
        // 初始化线程池
        executor.initialize();

        // 添加钩子，在JVM关闭时优雅地关闭线程池
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("关闭熔断线程池...");
            executor.shutdown();
            try {
                if (!executor.getThreadPoolExecutor().awaitTermination(23, TimeUnit.SECONDS)) {
                    logger.warn("熔断线程池未在23秒内完全关闭，强制关闭");
                    executor.getThreadPoolExecutor().shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.warn("熔断线程池关闭过程中发生中断", e);
                executor.getThreadPoolExecutor().shutdownNow();
                Thread.currentThread().interrupt();
            }
            logger.info("熔断线程池已关闭");
        }));

        logger.info("熔断线程池准备完毕，核心线程数: {}, 最大线程数: {}, 队列容量: {}", 2, 4, 96);
    }


    /**
     * 延时队列
     */
    private DelayQueue<MethodWrapper> circuitMethodHolder = new DelayQueue<>();

    /**
     * 熔断的方法
     */
    private Set<Integer> methodSet = new HashSet<>();

    private HHYCircuitBreakerProperties properties;

    public HHYCircuitBreakerController(HHYCircuitBreakerProperties properties) {
        this.properties = properties;
        run();
    }

    public void run() {
        Thread circuitListener = new Thread(() -> {
            logger.info("熔断监听线程已启动");

            while (true) {
                try {
                    logger.debug("等待获取熔断方法...");
                    MethodWrapper methodWrapper = circuitMethodHolder.take();
                    logger.info("开始处理熔断方法: {}.{}",
                            methodWrapper.getMethod().getDeclaringClass().getSimpleName(),
                            methodWrapper.getMethod().getName());

                    executor.execute(() -> {
                        logger.debug("在独立线程中执行熔断方法: {}", methodWrapper.getMethod().getName());

                        if (methodWrapper.invoke()) {
                            logger.info("方法调用成功，移除熔断标识: {}", methodWrapper.getMethod().getName());
                            final Method method = methodWrapper.getMethod();
                            // TODO: 只有被熔断方法被及时调用成功才会移除熔断记录，导致被熔断的服务重启后无法被当前进程感知，造成永久熔断，需要改进
                            methodSet.remove(method.hashCode());
                        } else {
                            logger.warn("方法调用失败: {}", methodWrapper.getMethod().getName());
                            // 调用失败，说明服务仍然不可用，准备重试
                            if (methodWrapper.incrAndGetRetryCount() >= properties.getMaxRetry()) {
                                logger.error("达到最大重试次数，触发告警: {}", methodWrapper.getMethod().getName());
                                IAlertService alerter = applicationContext.getBean(properties.getAlertName(), IAlertService.class);
                                alerter.alert(methodWrapper);
                            } else {
                                long retryInterval = properties.getRetryInterval();
                                logger.info("方法 [{}] 第 {} 次重试，间隔 {} ms",
                                        methodWrapper.getMethod().getName(),
                                        methodWrapper.getRetriedCount(),
                                        retryInterval);

                                methodWrapper.setExpire(System.currentTimeMillis() + retryInterval);
                                circuitMethodHolder.add(methodWrapper);
                            }
                        }
                    });
                } catch (Exception e) {
                    logger.error("熔断监听线程发生异常", e);
                    throw new RuntimeException(e);
                }
            }
        }, "circuit-listener-");
        circuitListener.setDaemon(true);
        circuitListener.start();
        logger.info("熔断监听线程已设置为守护线程，并启动");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void trigger(MethodWrapper methodInvoker) {
        methodInvoker.setExpire(System.currentTimeMillis() + properties.getRetryInterval());
        methodSet.add(methodInvoker.getMethod().hashCode());
        circuitMethodHolder.offer(methodInvoker);
    }

    @Override
    public boolean exist(int methodHashCode) {
        return methodSet.contains(methodHashCode);
    }

}
