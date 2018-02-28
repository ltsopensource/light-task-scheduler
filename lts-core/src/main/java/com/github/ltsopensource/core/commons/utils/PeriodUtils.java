package com.github.ltsopensource.core.commons.utils;

import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.support.SystemClock;

/**
 * @author Robert HG (254963746@qq.com) on 11/1/16.
 */
public class PeriodUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeriodUtils.class);
    private static final ThreadLocal<Period> TL = new ThreadLocal<Period>();

    public static void start() {
        if (!SystemPropertyUtils.isEnablePeriod()) {
            return;
        }
        Period period = new Period();
        period.start = SystemClock.now();
        TL.set(period);
    }

    public static void end(String msg, Object... args) {
        if (!SystemPropertyUtils.isEnablePeriod()) {
            return;
        }
        Period period = TL.get();
        if (period == null) {
            throw new IllegalStateException("please start first");
        }
        long mills = SystemClock.now() - period.start;
        TL.remove();
        LOGGER.warn("[Period]" + msg + ", mills:{}", args, mills);
    }

    private static class Period {
        private long start;
    }
}
