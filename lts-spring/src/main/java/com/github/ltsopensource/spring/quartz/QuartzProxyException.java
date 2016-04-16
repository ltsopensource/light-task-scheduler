package com.github.ltsopensource.spring.quartz;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/16.
 */
public class QuartzProxyException extends RuntimeException {

    public QuartzProxyException() {
        super();
    }

    public QuartzProxyException(String message) {
        super(message);
    }

    public QuartzProxyException(String message, Throwable cause) {
        super(message, cause);
    }

    public QuartzProxyException(Throwable cause) {
        super(cause);
    }
}
