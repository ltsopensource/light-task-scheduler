package com.lts.core.commons.utils;


import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;

/**
 * @author Robert HG (254963746@qq.com) on 9/26/15.
 */
public class QuietUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuietUtils.class);

    public static void doWithError(Callable callable) {
        try {
            callable.call();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static void doWithWarn(Callable callable) {
        try {
            callable.call();
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    public static void doWithInfo(Callable callable) {
        try {
            callable.call();
        } catch (Exception e) {
            LOGGER.info(e.getMessage(), e);
        }
    }

    public static void doQuietly(Callable callable) {
        try {
            callable.call();
        } catch (Exception ignored) {
        }
    }

    public interface Callable {
        void call() throws Exception;
    }

}
