package com.github.berbatov001.enhancedzuul.sentinel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 限流的配置类
 *
 * sentinel.enable 是否开始限流，默认关闭。
 * sentinel.behavior 别限流请求的处理方式 0-直接失败（默认） 1-预热  2-排队等待
 * sentinel.maxQueueingTimeMs 排队等待的最大时间，单位毫秒。(只有在sentinel.behavior=2的时候有效)
 * sentinel.warmUpPeriodSec 预热时间，单位秒。(只有在sentinel.behavior=1的时候有效)
 * sentinel.uniqueEntranceQPSThreshold 网关总入口的QPS阈值，默认1000.
 *
 * @author berbatov001
 */
@ConfigurationProperties("sentinel")
public class SentinelProperties {

    @Value("${sentinel.enable:false}")
    private boolean enable;
    @Value("${sentinel.behavior:0}")
    private int behavior;
    @Value("${sentinel.maxQueueingTimeMs:500}")
    private int maxQueueingTimeMs;
    @Value("${sentinel.warmUpPeriodSec:10}")
    private int warmUpPeriodSec;
    @Value("${sentinel.uniqueEntranceQPSThreshold:1000}")
    private int uniqueEntranceQPSThreshold;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getBehavior() {
        return behavior;
    }

    public void setBehavior(int behavior) {
        this.behavior = behavior;
    }

    public int getMaxQueueingTimeMs() {
        return maxQueueingTimeMs;
    }

    public void setMaxQueueingTimeMs(int maxQueueingTimeMs) {
        this.maxQueueingTimeMs = maxQueueingTimeMs;
    }

    public int getWarmUpPeriodSec() {
        return warmUpPeriodSec;
    }

    public void setWarmUpPeriodSec(int warmUpPeriodSec) {
        this.warmUpPeriodSec = warmUpPeriodSec;
    }

    public int getUniqueEntranceQPSThreshold() {
        return uniqueEntranceQPSThreshold;
    }

    public void setUniqueEntranceQPSThreshold(int uniqueEntranceQPSThreshold) {
        this.uniqueEntranceQPSThreshold = uniqueEntranceQPSThreshold;
    }
}
