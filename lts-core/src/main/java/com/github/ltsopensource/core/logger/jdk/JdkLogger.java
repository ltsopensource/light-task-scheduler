package com.github.ltsopensource.core.logger.jdk;

import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.support.AbstractLogger;

import java.util.logging.Level;

public class JdkLogger extends AbstractLogger implements Logger {

	private final java.util.logging.Logger logger;

	public JdkLogger(java.util.logging.Logger logger) {
		this.logger = logger;
	}

    public void trace(String msg) {
        logger.log(Level.FINER, msg);
    }

    public void trace(Throwable e) {
        logger.log(Level.FINER, e.getMessage(), e);
    }

    public void trace(String msg, Throwable e) {
        logger.log(Level.FINER, msg, e);
    }

	public void debug(String msg) {
		logger.log(Level.FINE, msg);
	}

    public void debug(Throwable e) {
        logger.log(Level.FINE, e.getMessage(), e);
    }

	public void debug(String msg, Throwable e) {
		logger.log(Level.FINE, msg, e);
	}

	public void info(String msg) {
		logger.log(Level.INFO, msg);
	}

	public void info(String msg, Throwable e) {
		logger.log(Level.INFO, msg, e);
	}

	public void warn(String msg) {
		logger.log(Level.WARNING, msg);
	}

	public void warn(String msg, Throwable e) {
		logger.log(Level.WARNING, msg, e);
	}

	public void error(String msg) {
		logger.log(Level.SEVERE, msg);
	}

	public void error(String msg, Throwable e) {
		logger.log(Level.SEVERE, msg, e);
	}

    public void error(Throwable e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
    }

    public void info(Throwable e) {
        logger.log(Level.INFO, e.getMessage(), e);
    }

    public void warn(Throwable e) {
        logger.log(Level.WARNING, e.getMessage(), e);
    }

    public boolean isTraceEnabled() {
        return logger.isLoggable(Level.FINER);
    }

	public boolean isDebugEnabled() {
		return logger.isLoggable(Level.FINE);
	}

	public boolean isInfoEnabled() {
		return logger.isLoggable(Level.INFO);
	}

	public boolean isWarnEnabled() {
		return logger.isLoggable(Level.WARNING);
	}

	public boolean isErrorEnabled() {
		return logger.isLoggable(Level.SEVERE);
	}

}