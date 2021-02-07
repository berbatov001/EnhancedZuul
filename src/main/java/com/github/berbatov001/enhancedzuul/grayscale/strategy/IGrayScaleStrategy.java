package com.github.berbatov001.enhancedzuul.grayscale.strategy;


import java.util.Map;

/**
 * 网关灰度策略
 *
 * @author berbatov001
 */
public interface IGrayScaleStrategy {

    /**
     * 网关灰度策略
     *
     * @param object
     * @return 下游所有服务的路由链路 serviceName ---> instanceVersion
     */
    Map<String, String> chooseGrayScaleChain(Object object);

    String type();
}
