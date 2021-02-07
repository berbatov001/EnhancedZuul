package com.github.berbatov001.enhancedzuul.grayscale.strategy;

import com.github.berbatov001.enhancedzuul.grayscale.support.GrayScaleProperties;
import com.github.berbatov001.enhancedzuul.netflix.zuul.support.FilterConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * IP灰度策略
 *
 * @author berbatov001
 */
public class IpGrayScaleStrategy implements IGrayScaleStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpGrayScaleStrategy.class);

    private GrayScaleProperties grayScaleProperties;

    public IpGrayScaleStrategy(GrayScaleProperties grayScaleProperties) {
        this.grayScaleProperties = grayScaleProperties;
    }

    @Override
    public Map<String, String> chooseGrayScaleChain(Object object) {
        boolean isMatched = false;
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes != null) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            String xForwardedForValue = request.getHeader(FilterConstants.X_FORWARDED_FOR_HEADER);
            LOGGER.info("本次请求头中的X-Forwarded-For={}", xForwardedForValue);
            if (StringUtils.hasText(xForwardedForValue)) {
                String[] realIpSet = xForwardedForValue.split(",");
                String pilotIpRegion = grayScaleProperties.getStrategyRule();
                LOGGER.info("从配置中获取到的pilotIpRegion={}", pilotIpRegion);
                String[] pilotIpSet = pilotIpRegion.split(",");
                outer:
                for (String realIp : realIpSet) {
                    realIp = realIp.trim();
                    for (String pilotIp : pilotIpSet) {
                        int wildcardFirstIndex = pilotIp.indexOf("*");
                        if (wildcardFirstIndex > 0) {
                            String ipPrefix = pilotIp.substring(0, wildcardFirstIndex);
                            if (realIp.startsWith(ipPrefix)) {
                                LOGGER.info("{}以{}开头，匹配成功。", realIp, ipPrefix);
                                isMatched = true;
                                break outer;
                            }
                        } else {
                            if (pilotIp.equals(realIp)) {
                                LOGGER.info("{}以{}开头，匹配成功。", realIp, pilotIp);
                                isMatched = true;
                                break outer;
                            }
                        }
                    }
                }
            }
        }
        return isMatched ? grayScaleProperties.getRoutersForValidation() : grayScaleProperties.getRoutersForOriginal();
    }

    @Override
    public String type() {
        return "IP";
    }
}
