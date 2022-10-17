package com.github.berbatov001.enhancedzuul.netflix.zuul.filters.pre;

import com.github.berbatov001.enhancedzuul.grayscale.strategy.IGrayScaleStrategy;
import com.github.berbatov001.enhancedzuul.grayscale.support.GrayScaleChain;
import com.github.berbatov001.enhancedzuul.grayscale.support.GrayScaleProperties;
import com.github.berbatov001.enhancedzuul.netflix.zuul.support.FilterConstants;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.exception.ZuulException;
import org.apache.tomcat.util.http.MimeHeaders;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 灰度Filter
 */
public class GraySaleReleaseFilter extends ZuulFilter implements SmartInitializingSingleton {

    @Autowired
    private GrayScaleProperties grayScaleProperties;

    @Autowired
    private List<IGrayScaleStrategy> grayScaleStrategyList;

    /**
     * 用来存放内存中所有灰度策略，key是策略Type。
     */
    private Map<String, IGrayScaleStrategy> placedStrategies = new HashMap<>(16);

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.GRAY_SALE_RELEASE_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        return grayScaleProperties.isEnable();
    }

    @Override
    public Object run() throws ZuulException {
        String presentStrategyType = grayScaleProperties.getStrategy();
        if (StringUtils.hasLength(presentStrategyType)) {
            presentStrategyType.toLowerCase();
        }
        IGrayScaleStrategy strategy = placedStrategies.get(presentStrategyType);
        if (strategy != null) {
            GrayScaleChain chosenGraySacleChain = strategy.chooseGrayScaleChain(null);
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = servletRequestAttributes.getRequest();
            boolean isChainType = "chain".equals(grayScaleProperties.getType());
            addToHeader(request, chosenGraySacleChain, isChainType);
        } else {
            String errorMessage = String.format("找不到指定的灰度策略%s！目前已有的策略集是%s。", presentStrategyType, placedStrategies);
            throw new ZuulException(errorMessage, -100, null);
        }
        return null;
    }

    @Override
    public void afterSingletonsInstantiated() {
        grayScaleStrategyList.forEach(strategy -> {
            String type = strategy.type().toLowerCase();
            placedStrategies.put(type, strategy);
        });
    }

    private void addToHeader(HttpServletRequest request, GrayScaleChain chosenGrayScaleChain, boolean isChainType) throws ZuulException {
        Map<String, String> supplement = chosenGrayScaleChain.getChosenGrayScaleChain();
        Class<? extends HttpServletRequest> requestClass = request.getClass();
        try {
            Field request1 = requestClass.getDeclaredField("request");
            request1.setAccessible(true);
            Object o = request1.get(request);
            Field coyoteRequest = o.getClass().getDeclaredField("coyoteRequest");
            coyoteRequest.setAccessible(true);
            Object o1 = coyoteRequest.get(o);
            Field headers = o1.getClass().getDeclaredField("headers");
            headers.setAccessible(true);
            MimeHeaders o2 = (MimeHeaders) headers.get(o1);
            supplement.forEach((key, value) -> {
                o2.addValue(key).setString(value);
            });
            o2.addValue("greEnable").setString("true");
            o2.addValue("isGreyRequest").setString(Boolean.toString(chosenGrayScaleChain.isGrayChain()));
            if (isChainType) {
                o2.addValue("greyType").setString("chain");
            }
        } catch (Exception e) {
            throw new ZuulException("向Header中添加属性时发生错误。", -200, "原始错误信息是：" + e.getMessage());
        }
    }
}
