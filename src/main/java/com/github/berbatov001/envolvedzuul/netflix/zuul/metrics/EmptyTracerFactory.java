package com.github.berbatov001.envolvedzuul.netflix.zuul.metrics;

import com.netflix.zuul.monitoring.Tracer;
import com.netflix.zuul.monitoring.TracerFactory;

public class EmptyTracerFactory extends TracerFactory {
    private final EmptyTracer emptytracer = new EmptyTracer();

    @Override
    public Tracer startMicroTracer(String s) {
        return emptytracer;
    }

    private static final class EmptyTracer implements Tracer {
        @Override
        public void stopAndLog() {

        }

        @Override
        public void setName(String s) {

        }
    }

}
