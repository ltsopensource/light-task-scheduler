package com.github.ltsopensource.alarm;

/**
 * @author Robert HG (254963746@qq.com) on 2/17/16.
 */
public class AlarmNotifyException extends RuntimeException {

    public AlarmNotifyException() {
        super();
    }

    public AlarmNotifyException(String message) {
        super(message);
    }

    public AlarmNotifyException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlarmNotifyException(Throwable cause) {
        super(cause);
    }
}
