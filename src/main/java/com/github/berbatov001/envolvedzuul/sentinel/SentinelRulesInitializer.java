package com.github.berbatov001.envolvedzuul.sentinel;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.List;

import static com.alibaba.csp.sentinel.slots.block.RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER;
import static com.alibaba.csp.sentinel.slots.block.RuleConstant.CONTROL_BEHAVIOR_WARM_UP;

public class SentinelRulesInitializer implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(SentinelRulesInitializer.class);

    public static final String UNIQUE_ENTRANCE_RESOURCE = "ZuulUniqueEntrance";

    private volatile boolean virgin = true;

    private SentinelProperties sentinelProperties;

    private int sentinelPropertiesVersion;

    SentinelRulesInitializer(SentinelProperties sentinelProperties) {
        this.sentinelProperties = sentinelProperties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initFlowRules();
    }

    public void initFlowRules() {
        try {
            int latestSentinelPropertiesVersion = sentinelProperties.toString().hashCode();
            if ((latestSentinelPropertiesVersion ^ sentinelPropertiesVersion) == 0) {
                //如果当前最新的版本和上一次一致，不做更新。
                return;
            }
            if (virgin) {
                //第一次程序启动的时候，之后enable为true才创建规则，否则不创建。
                if (sentinelProperties.isEnable()) {
                    List<FlowRule> rules = new ArrayList<>();
                    fillFlowRuleList(rules);
                    FlowRuleManager.loadRules(rules);
                    virgin = false;
                    //记录最新版本
                    sentinelPropertiesVersion = latestSentinelPropertiesVersion;
                }
            } else {
                List<FlowRule> rules = new ArrayList<>();
                if(sentinelProperties.isEnable()){
                    fillFlowRuleList(rules);
                }
                FlowRuleManager.loadRules(rules);
                //记录最新版本
                sentinelPropertiesVersion = latestSentinelPropertiesVersion;
            }
        } catch (Exception e) {
            LOGGER.error("初始化限流策略失败。：" + e.getMessage(), e);
        }
    }

    private void fillFlowRuleList(List<FlowRule> rules) {
        FlowRule zuulEntranceRule = createQPSRule(UNIQUE_ENTRANCE_RESOURCE, sentinelProperties.getUniqueEntranceQPSThreshold());
        rules.add(zuulEntranceRule);
    }

    public FlowRule createQPSRule(String resourceName, double threshold) {
        FlowRule flowRule = new FlowRule();
        flowRule.setRefResource(resourceName);
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setCount(threshold);
        if (sentinelProperties.getBehavior() == 1) {
            //预热
            int warmUpPeriodSec = sentinelProperties.getWarmUpPeriodSec();
            flowRule.setControlBehavior(CONTROL_BEHAVIOR_WARM_UP);
            flowRule.setWarmUpPeriodSec(warmUpPeriodSec);
        } else if (sentinelProperties.getBehavior() == 2) {
            //排队等待
            int maxQueueingTimeMs = sentinelProperties.getMaxQueueingTimeMs();
            flowRule.setControlBehavior(CONTROL_BEHAVIOR_RATE_LIMITER);
            flowRule.setMaxQueueingTimeMs(maxQueueingTimeMs);
        }
        return flowRule;
    }
}
