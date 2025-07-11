package com.hhy.circuitbreaker.alert;

import com.hhy.circuitbreaker.MethodWrapper;

/**
 * <p>
 * 描述: 告警器接口
 * </p>
 *
 * @Author hhy
 */
public interface IAlertService {
    void alert(MethodWrapper methodWrapper);
}
