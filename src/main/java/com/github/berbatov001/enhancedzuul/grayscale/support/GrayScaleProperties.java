package com.github.berbatov001.enhancedzuul.grayscale.support;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 读取灰度的配置文件参数
 *
 * @author berbatov001
 */
public class GrayScaleProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrayScaleProperties.class);

    @NacosValue(value = "${gray.enable:false}", autoRefreshed = true)
    private boolean enable;
    @NacosValue(value = "${gray.type:unknown}")
    private String type = "";
    @NacosValue(value = "${gray.strategy:unknown}", autoRefreshed = true)
    private String strategy = "";
    @NacosValue(value = "${gray.grayScaleReleaseRoute:unknown}", autoRefreshed = true)
    private String grayScaleReleaseRoute = "";
    @NacosValue(value = "${gray.strategyRule:unknown}", autoRefreshed = true)
    private String strategyRule = "";
    /**
     * 正常流量的链路
     */
    private Map<String, String> routersForOriginal = new HashMap<>();
    /**
     * 用于灰度验证的链路
     */
    private Map<String, String> routersForValidation = new HashMap<>();
    private int grayScaleReleaseRouteVersion;

    public boolean isEnable() {
        return enable;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStrategy() {
        return strategy;
    }

    public String getStrategyRule() {
        return strategyRule;
    }

    public Map<String, String> getRoutersForOriginal() {
        return routersForOriginal;
    }

    public Map<String, String> getRoutersForValidation() {
        return routersForValidation;
    }

    @PostConstruct
    public void init() {
        freshRouters();
    }

    /**
     * 如果本地和Nacos都不配置gray.grayScaleReleaseRoute，默认时unknown。
     * 如果配置了gray.grayScaleReleaseRoute=，但是等号后面不配置具体内容，则gray.grayScaleReleaseRoute=""，空串。
     * 如果nacos中之前配置了gray.grayScaleReleaseRoute，但是又注释掉了，则内存中grayScaleReleaseRoute属性还是原来的值。
     * （在nacos配置中心上注释掉的、删除的属性，@NacosValue不做处理，所以还是原来的值。）
     */
    public void freshRouters() {
        if (this.grayScaleReleaseRoute.trim().length() > 0 && !"unknown".equals(this.grayScaleReleaseRoute)) {
            int latestGrayScaleReleaseRoute = grayScaleReleaseRoute.hashCode();
            if ((latestGrayScaleReleaseRoute ^ grayScaleReleaseRouteVersion) == 0) {
                return;
            }
            try {
                JSONObject jsonObject = JSONObject.parseObject(this.grayScaleReleaseRoute);
                String baseRouteJson = jsonObject.get("base_route").toString();
                GrayScaleRoute baseRoute = JSONObject.parseObject(baseRouteJson, GrayScaleRoute.class);
                routersForOriginal = baseRoute.routers;

                String grayRouteJson = jsonObject.get("gray_route").toString();
                GrayScaleRoute grayRoute = JSONObject.parseObject(grayRouteJson, GrayScaleRoute.class);
                routersForValidation = grayRoute.routers;

                //更新grayScaleReleaseRoute版本
                grayScaleReleaseRouteVersion = latestGrayScaleReleaseRoute;
            } catch (Exception e) {
                LOGGER.error("解析Nacos灰度路由失败！错误信息：{}", e.getMessage(), e);
                throw new RuntimeException("解析Nacos灰度路由失败！");
            }
        }

    }

    public static class GrayScaleRoute {
        private String name;
        private Map<String, String> routers;

        public void setName(String name) {
            this.name = name;
        }

        public void setRouters(Map<String, String> routers) {
            this.routers = routers;
        }
    }


}
