package com.hhy.circuitbreaker.alert;

import com.hhy.circuitbreaker.MethodWrapper;

/**
 * <p>
 * 描述: 默认告警器
 * </p>
 *
 * @Author hhy
 */
public class DefaultAlert implements IAlertService{
    @Override
    public void alert(MethodWrapper methodWrapper) {
        System.out.println("告警了");
    }
}
