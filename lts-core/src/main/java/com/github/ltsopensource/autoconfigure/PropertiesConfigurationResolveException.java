package com.github.ltsopensource.autoconfigure;

/**
 * @author Robert HG (254963746@qq.com) on 4/20/16.
 */
public class PropertiesConfigurationResolveException extends RuntimeException {

    public PropertiesConfigurationResolveException() {
        super();
    }

    public PropertiesConfigurationResolveException(String message) {
        super(message);
    }

    public PropertiesConfigurationResolveException(String message, Throwable cause) {
        super(message, cause);
    }

    public PropertiesConfigurationResolveException(Throwable cause) {
        super(cause);
    }
}
