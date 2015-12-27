package com.lts.core.commons.utils;

/**
 * @author Robert HG (254963746@qq.com) on 5/30/15.
 */
public class CommonUtils {

    public static String exceptionSimpleDesc(final Throwable t) {
        StringBuilder sb = new StringBuilder();
        if (t != null) {
            sb.append(t.toString());

            StackTraceElement[] stackTrace = t.getStackTrace();
            if (stackTrace != null && stackTrace.length > 0) {
                StackTraceElement elment = stackTrace[0];
                sb.append(", ");
                sb.append(elment.toString());
            }
        }
        return sb.toString();
    }
}
