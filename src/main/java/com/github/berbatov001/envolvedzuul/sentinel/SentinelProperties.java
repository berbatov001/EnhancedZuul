package com.github.berbatov001.envolvedzuul.sentinel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
