package com.lts.job.core.util;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 */
public class StringUtils {

    private StringUtils() {
    }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().equals("");
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    public static String generateUUID() {
        return StringUtils.replace(java.util.UUID.randomUUID().toString(), "-", "").toUpperCase();
    }

    public static String replace(String text, String repl, String with) {
        return replace(text, repl, with, -1);
    }

    public static String replace(String text, String repl, String with, int max) {
        if (isEmpty(text) || isEmpty(repl) || with == null || max == 0) {
            return text;
        }
        int start = 0;
        int end = text.indexOf(repl, start);
        if (end == -1) {
            return text;
        }
        int replLength = repl.length();
        int increase = with.length() - replLength;
        increase = (increase < 0 ? 0 : increase);
        increase *= (max < 0 ? 16 : (max > 64 ? 64 : max));
        StringBuffer buf = new StringBuffer(text.length() + increase);
        while (end != -1) {
            buf.append(text.substring(start, end)).append(with);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = text.indexOf(repl, start);
        }
        buf.append(text.substring(start));
        return buf.toString();
    }

    public static boolean hasLength(String str) {
        return (str != null && str.length() > 0);
    }

    public static boolean hasText(String str) {
        if (!hasLength(str)) {
            return false;
        }
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static String format(String format, Object...args){
        FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
        return ft.getMessage();
    }
}
