package com.github.ltsopensource.core.logger.support;

/**
 * 来自 slf4j
 * @author Robert HG (254963746@qq.com) on 5/19/15.
 */
public class FormattingTuple {

    static public FormattingTuple NULL = new FormattingTuple(null);

    private String message;
    private Throwable throwable;
    private Object[] argArray;

    public FormattingTuple(String message) {
        this(message, null, null);
    }

    public FormattingTuple(String message, Object[] argArray, Throwable throwable) {
        this.message = message;
        this.throwable = throwable;
        if(throwable == null) {
            this.argArray = argArray;
        } else {
            this.argArray = trimmedCopy(argArray);
        }
    }

    static Object[] trimmedCopy(Object[] argArray) {
        if(argArray == null || argArray.length == 0) {
            throw new  IllegalStateException("non-sensical empty or null argument array");
        }
        final int trimemdLen = argArray.length -1;
        Object[] trimmed = new Object[trimemdLen];
        System.arraycopy(argArray, 0, trimmed, 0, trimemdLen);
        return trimmed;
    }

    public String getMessage() {
        return message;
    }

    public Object[] getArgArray() {
        return argArray;
    }

    public Throwable getThrowable() {
        return throwable;
    }

}
