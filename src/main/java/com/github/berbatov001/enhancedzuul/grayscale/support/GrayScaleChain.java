package com.github.berbatov001.enhancedzuul.grayscale.support;

import java.util.HashMap;
import java.util.Map;

/**
 * 针对一次请求选择的下游链路
 *
 */
public class GrayScaleChain {
    /**
     * 本次请求选择的是否位灰度链路
     */
    private boolean isGrayChain;
    /**
     * 本次请求所选择的链路
     */
    private Map<String, String> chosenGrayScaleChain = new HashMap<>(16);

    public boolean isGrayChain() {
        return isGrayChain;
    }

    public void setGrayChain(boolean grayChain) {
        isGrayChain = grayChain;
    }

    public Map<String, String> getChosenGrayScaleChain() {
        return chosenGrayScaleChain;
    }

    public void setChosenGrayScaleChain(Map<String, String> chosenGrayScaleChain) {
        this.chosenGrayScaleChain = chosenGrayScaleChain;
    }
}
