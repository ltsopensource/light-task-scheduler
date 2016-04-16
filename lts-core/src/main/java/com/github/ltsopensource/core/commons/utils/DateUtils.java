package com.github.ltsopensource.core.commons.utils;

import com.github.ltsopensource.core.commons.time.FastDateFormat;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * @author Robert HG (254963746@qq.com) on 5/27/15.
 */
public class DateUtils {

    public static final String YMD_HMS = "yyyy-MM-dd HH:mm:ss";

    public static final String YMD = "yyyy-MM-dd";

    public static final long ONE_DAY_MillIS = 24 * 60 * 60 * 1000;

    private static final ConcurrentMap<Pattern, String> regPatternMap = new ConcurrentHashMap<Pattern, String>();

    static {
        regPatternMap.put(Pattern.compile("^\\d{4}-\\d{1,2}-\\d{1,2}$"), "yyyy-MM-dd");
        regPatternMap.put(Pattern.compile("^\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}$"), "yyyy-MM-dd HH:mm:ss");
        regPatternMap.put(Pattern.compile("^\\d{4}\\d{1,2}\\d{1,2}$"), "yyyyMMdd");
        regPatternMap.put(Pattern.compile("^\\d{4}\\d{1,2}$"), "yyyyMM");
        regPatternMap.put(Pattern.compile("^\\d{4}/\\d{1,2}/\\d{1,2}$"), "yyyy/MM/dd");
        regPatternMap.put(Pattern.compile("^\\d{4}年\\d{1,2}月\\d{1,2}日$"), "yyyy年MM月dd日");
        regPatternMap.put(Pattern.compile("^\\d{4}/\\d{1,2}/\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}$"), "yyyy/MM/dd HH:mm:ss");
        regPatternMap.put(Pattern.compile("^\\d{4}/\\d{1,2}/\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1}$"), "yyyy/MM/dd HH:mm:ss.S");
        regPatternMap.put(Pattern.compile("^\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1}$"), "yyyy-MM-dd HH:mm:ss.S");
    }

    public static Date convert(Long timestamp) {
        return new Date(timestamp);
    }

    /**
     * 方法描述：获取当前时间的
     */
    public static Date now() {
        return new Date();
    }

    public static Long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 方法描述：格式化日期
     */
    public static String formatDate(Date d, String fmt) {
        return FastDateFormat.getInstance(fmt).format(d);
    }

    public static String formatYMD(Date d) {
        return FastDateFormat.getInstance(YMD).format(d);
    }

    public static String formatYMD_HMS(Date d) {
        return FastDateFormat.getInstance(YMD_HMS).format(d);
    }

    public static String formatDate(Long date, String fmt) {
        return FastDateFormat.getInstance(fmt).format(date);
    }

    public static String format(Date date, String fmt) {
        return FastDateFormat.getInstance(fmt).format(date);
    }

    public static Date parse(String date, String fmt) {
        try {
            return FastDateFormat.getInstance(fmt).parse(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date parse(String date) {
        try {
            for (Map.Entry<Pattern, String> entry : regPatternMap.entrySet()) {
                boolean isMatch = entry.getKey().matcher(date).matches();
                if (isMatch) {
                    return FastDateFormat.getInstance(entry.getValue()).parse(date);
                }
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException("can't support this pattern , date is " + date);
        }
        throw new IllegalArgumentException("can't support this pattern , date is " + date);
    }

    public static Date parseYMD(String date) {
        try {
            return FastDateFormat.getInstance(YMD).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date parseYMD_HMS(String date) {
        try {
            return FastDateFormat.getInstance(YMD_HMS).parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException("the date pattern is error!");
        }
    }

    /**
     * 是否是同一天
     */
    public static boolean isSameDay(Date date, Date date2) {
        if (date == null || date2 == null) {
            return false;
        }
        FastDateFormat df = FastDateFormat.getInstance(YMD);
        return df.format(date).equals(df.format(date2));
    }

    public static Date addMonth(Date date, int interval) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, interval);
        return calendar.getTime();
    }

    public static Date addDay(Date date, int interval) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, interval);
        return calendar.getTime();
    }

    public static Date addHour(Date date, int interval) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, interval);
        return calendar.getTime();
    }

    public static Date addMinute(Date date, int interval) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, interval);
        return calendar.getTime();
    }

}
