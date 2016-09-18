package com.github.ltsopensource.core.commons.utils;


import com.github.ltsopensource.core.logger.support.FormattingTuple;
import com.github.ltsopensource.core.logger.support.MessageFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 */
public final class StringUtils {

    private StringUtils() {
    }

    private static final Pattern INT_PATTERN = Pattern.compile("^\\d+$");

    public static boolean isEmpty(String s) {
        return s == null || s.trim().equals("");
    }

    public static boolean isNotEmpty(String... strs) {
        if (strs != null) {
            for (String s : strs) {
                if (isEmpty(s)) {
                    return false;
                }
            }
            return true;
        }
        return false;
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
        StringBuilder buf = new StringBuilder(text.length() + increase);
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

    public static String format(String format, Object... args) {
        FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
        return ft.getMessage();
    }

    public static String toString(Throwable e) {
        StringWriter w = new StringWriter();
        PrintWriter p = new PrintWriter(w);
        p.print(e.getClass().getName());
        if (e.getMessage() != null) {
            p.print(": " + e.getMessage());
        }
        p.println();
        try {
            e.printStackTrace(p);
            return w.toString();
        } finally {
            p.close();
        }
    }

    public static String toString(String msg, Throwable e) {
        StringWriter w = new StringWriter();
        w.write(msg + "\n");
        PrintWriter p = new PrintWriter(w);
        try {
            e.printStackTrace(p);
            return w.toString();
        } finally {
            p.close();
        }
    }

    public static String concat(String... strings) {
        if (strings == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String str : strings) {
            if (str != null) {
                sb.append(str);
            }
        }
        return sb.toString();
    }

    public static String trim(String str) {
        if (str == null) {
            return null;
        }
        return str.trim();
    }

    public static boolean isInteger(String str) {
        return !(str == null || str.length() == 0) && INT_PATTERN.matcher(str).matches();
    }

    public static String toString(Object value) {
        if (value == null) {
            return null;
        }

        return value.toString();
    }

    public static String[] splitWithTrim(String spilt, String sequence) {
        if (isEmpty(sequence)) {
            return null;
        }
        String[] values = sequence.split(spilt);
        if (values.length == 0) {
            return values;
        }
        for (int i = 0; i < values.length; i++) {
            values[i] = values[i].trim();
        }
        return values;
    }

    /**
     * Capitalize a {@code String}, changing the first letter to
     * upper case as per {@link Character#toUpperCase(char)}.
     * No other letters are changed.
     * @param str the {@code String} to capitalize, may be {@code null}
     * @return the capitalized {@code String}, or {@code null} if the supplied
     * string is {@code null}
     */
    public static String capitalize(String str) {
        return changeFirstCharacterCase(str, true);
    }

    /**
     * Uncapitalize a {@code String}, changing the first letter to
     * lower case as per {@link Character#toLowerCase(char)}.
     * No other letters are changed.
     * @param str the {@code String} to uncapitalize, may be {@code null}
     * @return the uncapitalized {@code String}, or {@code null} if the supplied
     * string is {@code null}
     */
    public static String uncapitalize(String str) {
        return changeFirstCharacterCase(str, false);
    }

    private static String changeFirstCharacterCase(String str, boolean capitalize) {
        if (str == null || str.length() == 0) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str.length());
        if (capitalize) {
            sb.append(Character.toUpperCase(str.charAt(0)));
        }
        else {
            sb.append(Character.toLowerCase(str.charAt(0)));
        }
        sb.append(str.substring(1));
        return sb.toString();
    }
}
