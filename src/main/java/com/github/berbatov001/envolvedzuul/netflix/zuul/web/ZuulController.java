package com.github.berbatov001.envolvedzuul.netflix.zuul.web;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulRunner;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.github.berbatov001.envolvedzuul.netflix.zuul.support.FilterConstants.REMOTE_RESULT_KEY;
import static com.github.berbatov001.envolvedzuul.sentinel.SentinelRulesInitializer.UNIQUE_ENTRANCE_RESOURCE;

@ResponseBody
public class ZuulController {

    private ZuulRunner zuulRunner;

    private ObjectMapper objectMapper;

    public ZuulController() {
        zuulRunner = new ZuulRunner(false);
        objectMapper = Jackson2ObjectMapperBuilder.json().build();
    }

    @SentinelResource(value = UNIQUE_ENTRANCE_RESOURCE, blockHandler = "zuulUniqueEntranceHandle", blockHandlerClass = BlockException.class)
    public Object handleRequest(HttpServletRequest request, HttpServletResponse response) throws ZuulException {
        try {
            zuulRunner.init(request, response);
            RequestContext context = RequestContext.getCurrentContext();
            context.setZuulEngineRan();
            try {
                preRoute();
            } catch (ZuulException e) {
                error(e);
                postRoute();
                throw e.getCause() != null ? e.getCause() : e;
            }
            try {
                route();
            } catch (ZuulException e) {
                error(e);
                postRoute();
                throw e.getCause() != null ? e.getCause() : e;
            }
            try {
                postRoute();
            } catch (ZuulException e) {
                error(e);
                postRoute();
                throw e.getCause() != null ? e.getCause() : e;
            }
            return context.get(REMOTE_RESULT_KEY);
        } catch (Throwable throwable) {
            throw new ZuulException(throwable, 500, "无法处理的异常：" + throwable.getClass().getName());
        } finally {
            RequestContext.getCurrentContext().unset();
        }
    }

    private void preRoute() throws ZuulException {
        zuulRunner.preRoute();
    }

    private void route() throws ZuulException{
        zuulRunner.route();
    }

    private void postRoute() throws ZuulException {
        zuulRunner.postRoute();
    }

    private void error(ZuulException e) {
        RequestContext.getCurrentContext().setThrowable(e);
        zuulRunner.error();
    }

}
