package com.github.berbatov001.envolvedzuul.grayscale.filters.pre;

import com.github.berbatov001.envolvedzuul.grayscale.strategy.IGrayScaleStrategy;
import com.github.berbatov001.envolvedzuul.grayscale.support.GrayScaleProperties;
import com.github.berbatov001.envolvedzuul.netflix.zuul.support.FilterConstants;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraySaleReleaseFilter extends ZuulFilter implements SmartInitializingSingleton {

    @Autowired
    private GrayScaleProperties grayScaleProperties;

    @Autowired
    private List<IGrayScaleStrategy> grayScaleStrategyList;

    private Map<String, IGrayScaleStrategy> placedStrategies = new HashMap<>(16);

    @Override
    public String filterType() {
        return null;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.GRAY_SALE_RELEASE_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        return false;
    }

    @Override
    public Object run() throws ZuulException {
        return null;
    }

    @Override
    public void afterSingletonsInstantiated() {

    }
}
