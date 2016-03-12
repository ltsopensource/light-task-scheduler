package com.lts.core.support;

import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 非守护线程, 保持节点存活
 * @author Robert HG (254963746@qq.com) on 3/11/16.
 */
public class AliveKeeping {

    private static final Logger LOGGER = LoggerFactory.getLogger(AliveKeeping.class);

    private final static Timer timer = new Timer("AliveKeepingService");

    private static AtomicBoolean start = new AtomicBoolean(false);

    public static void start() {
        if (start.compareAndSet(false, true)) {
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("I'm alive");
                    }
                }
            }, 1000 * 60 * 10, 1000 * 60 * 10);
        }
    }

    public static void stop() {
        if (start.compareAndSet(true, false)) {
            timer.cancel();
        }
    }
}
