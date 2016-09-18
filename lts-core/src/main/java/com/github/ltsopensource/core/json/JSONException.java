package com.github.ltsopensource.core.json;

/**
 * @author Robert HG (254963746@qq.com) on 11/19/15.
 */
public class JSONException extends RuntimeException {

    public JSONException() {
        super();
    }

    public JSONException(String message) {
        super(message);
    }

    public JSONException(String message, Throwable cause) {
        super(message, cause);
    }

    public JSONException(Throwable cause) {
        super(cause);
    }
}
