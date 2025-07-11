package com.hhy.circuitbreaker;

/**
 * <p>
 * 描述: 熔断裁决器
 * </p>
 *
 * @Author hhy
 */
public class CircuitBreakerArbiter {
    /**
     * 成功数
     */
    private int successCount;

    /**
     * 失败数
     */
    private int failCount;

    public CircuitBreakerArbiter() {
        this.successCount = 0;
        this.failCount = 0;
    }

    /**
     * 决断是否需要熔断
     *
     * @param failRateLimit 可容忍的最大失败率
     * @return
     */
    public boolean isFailureRateTolerable(double failRateLimit) {
        int totalRequests = successCount + failCount;
        // 真实错误率
        double actualSuccessRate = (double) failCount / totalRequests;
        return failRateLimit >= actualSuccessRate;
    }

    public void reset() {
        this.successCount = 0;
        this.failCount = 0;
    }
    public void incrSuccess(){
        this.successCount++;
    }

    public void incrFail(){
        this.failCount++;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }
}
