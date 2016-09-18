package com.github.ltsopensource.tasktracker.logger;

import com.github.ltsopensource.core.constant.Level;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;

/**
 * @author Robert HG (254963746@qq.com) on 9/12/15.
 */
public class MockBizLogger extends BizLoggerAdapter implements BizLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockBizLogger.class);
    private Level level;

    public MockBizLogger(Level level) {
        this.level = level;
        if (level == null) {
            this.level = Level.INFO;
        }
    }

    @Override
    public void debug(String msg) {
        if (level.ordinal() <= Level.DEBUG.ordinal()) {
            LOGGER.debug(msg);
        }
    }

    @Override
    public void info(String msg) {
        if (level.ordinal() <= Level.INFO.ordinal()) {
            LOGGER.info(msg);
        }
    }

    @Override
    public void error(String msg) {
        if (level.ordinal() <= Level.ERROR.ordinal()) {
            LOGGER.error(msg);
        }
    }
}
