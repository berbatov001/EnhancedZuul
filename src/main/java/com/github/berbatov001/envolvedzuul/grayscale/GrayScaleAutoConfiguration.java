package com.github.berbatov001.envolvedzuul.grayscale;

import com.github.berbatov001.envolvedzuul.grayscale.filters.pre.GraySaleReleaseFilter;
import com.github.berbatov001.envolvedzuul.grayscale.strategy.IGrayScaleStrategy;
import com.github.berbatov001.envolvedzuul.grayscale.strategy.IpGrayScaleStrategy;
import com.github.berbatov001.envolvedzuul.grayscale.support.GrayScaleProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 灰度配置类
 *
 * @author berbatov001
 */
@Configuration
public class GrayScaleAutoConfiguration {

    @Bean
    public GrayScaleProperties grayScaleProperties() {
        return new GrayScaleProperties();
    }

    @Bean
    public GraySaleReleaseFilter graySaleReleaseFilter() {
        return new GraySaleReleaseFilter();
    }

    @Bean
    public IGrayScaleStrategy ipGrayScaleStrategy() {
        return new IpGrayScaleStrategy(grayScaleProperties());
    }
}
