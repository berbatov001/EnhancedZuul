package com.github.berbatov001.envolvedzuul.netflix.zuul.metrics;

import com.netflix.zuul.monitoring.CounterFactory;

public class EmptyCounterFactory extends CounterFactory {
    @Override
    public void increment(String s) {

    }
}
