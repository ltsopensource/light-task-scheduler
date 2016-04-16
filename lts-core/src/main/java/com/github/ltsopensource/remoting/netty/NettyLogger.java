package com.github.ltsopensource.remoting.netty;

import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * @author Robert HG (254963746@qq.com) on 11/5/15.
 */
public class NettyLogger {

    public static void setNettyLoggerFactory() {
        InternalLoggerFactory factory = InternalLoggerFactory.getDefaultFactory();
        if (factory == null || !(factory instanceof LtsLoggerFactory)) {
            InternalLoggerFactory.setDefaultFactory(new LtsLoggerFactory());
        }
    }

    private static class LtsLoggerFactory extends InternalLoggerFactory {
        @Override
        protected InternalLogger newInstance(String name) {
            return new LtsLogger(name);
        }
    }

    static class LtsLogger implements InternalLogger {

        private Logger logger;
        private String name;

        LtsLogger(String name) {
            this.name = name;
            this.logger = LoggerFactory.getLogger(name);
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public boolean isTraceEnabled() {
            return logger.isTraceEnabled();
        }

        @Override
        public void trace(String msg) {
            logger.trace(msg);
        }

        @Override
        public void trace(String format, Object arg) {
            logger.trace(format, arg);
        }

        @Override
        public void trace(String format, Object argA, Object argB) {
            logger.trace(format, argA, argB);
        }

        @Override
        public void trace(String format, Object... arguments) {
            logger.trace(format, arguments);
        }

        @Override
        public void trace(String msg, Throwable t) {
            logger.trace(msg, t);
        }

        @Override
        public boolean isDebugEnabled() {
            return logger.isDebugEnabled();
        }

        @Override
        public void debug(String msg) {
            logger.debug(msg);
        }

        @Override
        public void debug(String format, Object arg) {
            logger.debug(format, arg);
        }

        @Override
        public void debug(String format, Object argA, Object argB) {
            logger.debug(format, argA, argB);
        }

        @Override
        public void debug(String format, Object... arguments) {
            logger.debug(format, arguments);
        }

        @Override
        public void debug(String msg, Throwable t) {
            logger.debug(msg, t);
        }

        @Override
        public boolean isInfoEnabled() {
            return logger.isInfoEnabled();
        }

        @Override
        public void info(String msg) {
            logger.info(msg);
        }

        @Override
        public void info(String format, Object arg) {
            logger.info(format, arg);
        }

        @Override
        public void info(String format, Object argA, Object argB) {
            logger.info(format, argA, argB);
        }

        @Override
        public void info(String format, Object... arguments) {
            logger.info(format, arguments);
        }

        @Override
        public void info(String msg, Throwable t) {
            logger.info(msg, t);
        }

        @Override
        public boolean isWarnEnabled() {
            return logger.isWarnEnabled();
        }

        @Override
        public void warn(String msg) {
            logger.warn(msg);
        }

        @Override
        public void warn(String format, Object arg) {
            logger.warn(format, arg);
        }

        @Override
        public void warn(String format, Object... arguments) {
            logger.warn(format, arguments);
        }

        @Override
        public void warn(String format, Object argA, Object argB) {
            logger.warn(format, argA, argB);
        }

        @Override
        public void warn(String msg, Throwable t) {
            logger.warn(msg, t);
        }

        @Override
        public boolean isErrorEnabled() {
            return logger.isErrorEnabled();
        }

        @Override
        public void error(String msg) {
            logger.error(msg);
        }

        @Override
        public void error(String format, Object arg) {
            logger.error(format, arg);
        }

        @Override
        public void error(String format, Object argA, Object argB) {
            logger.error(format, argA, argB);
        }

        @Override
        public void error(String format, Object... arguments) {
            logger.error(format, arguments);
        }

        @Override
        public void error(String msg, Throwable t) {
            logger.error(msg, t);
        }

        @Override
        public boolean isEnabled(InternalLogLevel level) {
            if (level == null) {
                return false;
            }
            switch (level) {
                case TRACE:
                    return logger.isTraceEnabled();
                case DEBUG:
                    return logger.isDebugEnabled();
                case INFO:
                    return logger.isInfoEnabled();
                case WARN:
                    return logger.isWarnEnabled();
                case ERROR:
                    return logger.isErrorEnabled();
            }
            return false;
        }

        @Override
        public void log(InternalLogLevel level, String msg) {
            if (level == null) {
                return;
            }
            switch (level) {
                case TRACE:
                    trace(msg);
                    break;
                case DEBUG:
                    debug(msg);
                    break;
                case INFO:
                    info(msg);
                    break;
                case WARN:
                    warn(msg);
                    break;
                case ERROR:
                    error(msg);
                    break;
            }
        }

        @Override
        public void log(InternalLogLevel level, String format, Object arg) {
            if (level == null) {
                return;
            }
            switch (level) {
                case TRACE:
                    trace(format, arg);
                    break;
                case DEBUG:
                    debug(format, arg);
                    break;
                case INFO:
                    info(format, arg);
                    break;
                case WARN:
                    warn(format, arg);
                    break;
                case ERROR:
                    error(format, arg);
                    break;
            }
        }

        @Override
        public void log(InternalLogLevel level, String format, Object argA, Object argB) {
            if (level == null) {
                return;
            }
            switch (level) {
                case TRACE:
                    trace(format, argA, argB);
                    break;
                case DEBUG:
                    debug(format, argA, argB);
                    break;
                case INFO:
                    info(format, argA, argB);
                    break;
                case WARN:
                    warn(format, argA, argB);
                    break;
                case ERROR:
                    error(format, argA, argB);
                    break;
            }
        }

        @Override
        public void log(InternalLogLevel level, String format, Object... arguments) {
            if (level == null) {
                return;
            }
            switch (level) {
                case TRACE:
                    trace(format, arguments);
                    break;
                case DEBUG:
                    debug(format, arguments);
                    break;
                case INFO:
                    info(format, arguments);
                    break;
                case WARN:
                    warn(format, arguments);
                    break;
                case ERROR:
                    error(format, arguments);
                    break;
            }
        }

        @Override
        public void log(InternalLogLevel level, String msg, Throwable t) {
            if (level == null) {
                return;
            }
            switch (level) {
                case TRACE:
                    trace(msg, t);
                    break;
                case DEBUG:
                    debug(msg, t);
                    break;
                case INFO:
                    info(msg, t);
                    break;
                case WARN:
                    warn(msg, t);
                    break;
                case ERROR:
                    error(msg, t);
                    break;
            }
        }
    }
}
