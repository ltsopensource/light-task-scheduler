package com.github.ltsopensource.core.commons.utils;

import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;

/**
 * @author Robert HG (254963746@qq.com) on 11/1/16.
 */
public class DotLogUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DotLogUtils.class);

    public static void dot(String msg, Object... args) {
        if (SystemPropertyUtils.isEnableDotLog()) {
            LOGGER.warn("[{}] " + msg, Thread.currentThread().getName(), args);
        }
    }
}
