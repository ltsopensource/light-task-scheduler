package com.github.ltsopensource.core.exception;

/**
 * @author Robert HG (254963746@qq.com) on 4/21/16.
 */
public class ConfigPropertiesIllegalException extends RuntimeException {

    public ConfigPropertiesIllegalException() {
        super();
    }

    public ConfigPropertiesIllegalException(String message) {
        super(message);
    }

    public ConfigPropertiesIllegalException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigPropertiesIllegalException(Throwable cause) {
        super(cause);
    }
}
