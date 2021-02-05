package com.github.berbatov001.envolvedzuul.sentinel;

import com.alibaba.csp.sentinel.slots.block.BlockException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 统一的限流异常处理类，请注意：
 * 1、这里的方法都必须时静态的。
 * 2、方法里面不能向外抛异常。
 * 3、方法参数列表的最后一个一定要是BlockException，不能省略。
 */
public class BlockExceptionHandler {

    private static final String BLOCKED_MESSAGE = "网关繁忙，请稍后再试，谢谢！";

    public static Object zuulUniqueEntranceHandle(HttpServletRequest request, HttpServletResponse response, BlockException exception) {
        return BLOCKED_MESSAGE;
    }
}
