package com.lts.job.core.commons.utils;

/**
 * @author Robert HG (254963746@qq.com) on 5/30/15.
 */
public class CommonUtils {

    public static String exceptionSimpleDesc(final Exception e) {
        StringBuffer sb = new StringBuffer();
        if (e != null) {
            sb.append(e.toString());

            StackTraceElement[] stackTrace = e.getStackTrace();
            if (stackTrace != null && stackTrace.length > 0) {
                StackTraceElement elment = stackTrace[0];
                sb.append(", ");
                sb.append(elment.toString());
            }
        }

        return sb.toString();
    }
}
