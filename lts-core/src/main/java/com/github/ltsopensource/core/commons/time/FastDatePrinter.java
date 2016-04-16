package com.github.ltsopensource.core.commons.time;

import com.github.ltsopensource.core.commons.utils.Assert;

import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * from commons-lang 为了性能
 */
public class FastDatePrinter {

    private final String mPattern;
    
    private final TimeZone mTimeZone;
    
    private final Locale mLocale;
    
    private transient Rule[] mRules;
    
    private transient int mMaxLengthEstimate;

    protected FastDatePrinter(final String pattern, final TimeZone timeZone, final Locale locale) {
        mPattern = pattern;
        mTimeZone = timeZone;
        mLocale = locale;

        init();
    }

    private void init() {
        final List<Rule> rulesList = parsePattern();
        mRules = rulesList.toArray(new Rule[rulesList.size()]);

        int len = 0;
        for (int i=mRules.length; --i >= 0; ) {
            len += mRules[i].estimateLength();
        }

        mMaxLengthEstimate = len;
    }

    protected List<Rule> parsePattern() {
        final DateFormatSymbols symbols = new DateFormatSymbols(mLocale);
        final List<Rule> rules = new ArrayList<Rule>();

        final String[] ERAs = symbols.getEras();
        final String[] months = symbols.getMonths();
        final String[] shortMonths = symbols.getShortMonths();
        final String[] weekdays = symbols.getWeekdays();
        final String[] shortWeekdays = symbols.getShortWeekdays();
        final String[] AmPmStrings = symbols.getAmPmStrings();

        final int length = mPattern.length();
        final int[] indexRef = new int[1];

        for (int i = 0; i < length; i++) {
            indexRef[0] = i;
            final String token = parseToken(mPattern, indexRef);
            i = indexRef[0];

            final int tokenLen = token.length();
            if (tokenLen == 0) {
                break;
            }

            Rule rule;
            final char c = token.charAt(0);

            switch (c) {
                case 'G': // era designator (text)
                    rule = new TextField(Calendar.ERA, ERAs);
                    break;
                case 'y': // year (number)
                    if (tokenLen == 2) {
                        rule = TwoDigitYearField.INSTANCE;
                    } else {
                        rule = selectNumberRule(Calendar.YEAR, tokenLen < 4 ? 4 : tokenLen);
                    }
                    break;
                case 'M': // month in year (text and number)
                    if (tokenLen >= 4) {
                        rule = new TextField(Calendar.MONTH, months);
                    } else if (tokenLen == 3) {
                        rule = new TextField(Calendar.MONTH, shortMonths);
                    } else if (tokenLen == 2) {
                        rule = TwoDigitMonthField.INSTANCE;
                    } else {
                        rule = UnpaddedMonthField.INSTANCE;
                    }
                    break;
                case 'd': // day in month (number)
                    rule = selectNumberRule(Calendar.DAY_OF_MONTH, tokenLen);
                    break;
                case 'h': // hour in am/pm (number, 1..12)
                    rule = new TwelveHourField(selectNumberRule(Calendar.HOUR, tokenLen));
                    break;
                case 'H': // hour in day (number, 0..23)
                    rule = selectNumberRule(Calendar.HOUR_OF_DAY, tokenLen);
                    break;
                case 'm': // minute in hour (number)
                    rule = selectNumberRule(Calendar.MINUTE, tokenLen);
                    break;
                case 's': // second in minute (number)
                    rule = selectNumberRule(Calendar.SECOND, tokenLen);
                    break;
                case 'S': // millisecond (number)
                    rule = selectNumberRule(Calendar.MILLISECOND, tokenLen);
                    break;
                case 'E': // day in week (text)
                    rule = new TextField(Calendar.DAY_OF_WEEK, tokenLen < 4 ? shortWeekdays : weekdays);
                    break;
                case 'D': // day in year (number)
                    rule = selectNumberRule(Calendar.DAY_OF_YEAR, tokenLen);
                    break;
                case 'F': // day of week in month (number)
                    rule = selectNumberRule(Calendar.DAY_OF_WEEK_IN_MONTH, tokenLen);
                    break;
                case 'w': // week in year (number)
                    rule = selectNumberRule(Calendar.WEEK_OF_YEAR, tokenLen);
                    break;
                case 'W': // week in month (number)
                    rule = selectNumberRule(Calendar.WEEK_OF_MONTH, tokenLen);
                    break;
                case 'a': // am/pm marker (text)
                    rule = new TextField(Calendar.AM_PM, AmPmStrings);
                    break;
                case 'k': // hour in day (1..24)
                    rule = new TwentyFourHourField(selectNumberRule(Calendar.HOUR_OF_DAY, tokenLen));
                    break;
                case 'K': // hour in am/pm (0..11)
                    rule = selectNumberRule(Calendar.HOUR, tokenLen);
                    break;
                case 'z': // time zone (text)
                    if (tokenLen >= 4) {
                        rule = new TimeZoneNameRule(mTimeZone, mLocale, TimeZone.LONG);
                    } else {
                        rule = new TimeZoneNameRule(mTimeZone, mLocale, TimeZone.SHORT);
                    }
                    break;
                case 'Z': // time zone (value)
                    if (tokenLen == 1) {
                        rule = TimeZoneNumberRule.INSTANCE_NO_COLON;
                    } else {
                        rule = TimeZoneNumberRule.INSTANCE_COLON;
                    }
                    break;
                case '\'': // literal text
                    final String sub = token.substring(1);
                    if (sub.length() == 1) {
                        rule = new CharacterLiteral(sub.charAt(0));
                    } else {
                        rule = new StringLiteral(sub);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Illegal pattern component: " + token);
            }

            rules.add(rule);
        }

        return rules;
    }

    
    protected String parseToken(final String pattern, final int[] indexRef) {
        final StringBuilder buf = new StringBuilder();

        int i = indexRef[0];
        final int length = pattern.length();

        char c = pattern.charAt(i);
        if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') {
            // Scan a run of the same character, which indicates a time
            // pattern.
            buf.append(c);

            while (i + 1 < length) {
                final char peek = pattern.charAt(i + 1);
                if (peek == c) {
                    buf.append(c);
                    i++;
                } else {
                    break;
                }
            }
        } else {
            // This will identify token as text.
            buf.append('\'');

            boolean inLiteral = false;

            for (; i < length; i++) {
                c = pattern.charAt(i);

                if (c == '\'') {
                    if (i + 1 < length && pattern.charAt(i + 1) == '\'') {
                        // '' is treated as escaped '
                        i++;
                        buf.append(c);
                    } else {
                        inLiteral = !inLiteral;
                    }
                } else if (!inLiteral &&
                        (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')) {
                    i--;
                    break;
                } else {
                    buf.append(c);
                }
            }
        }

        indexRef[0] = i;
        return buf.toString();
    }

    
    protected NumberRule selectNumberRule(final int field, final int padding) {
        switch (padding) {
            case 1:
                return new UnpaddedNumberField(field);
            case 2:
                return new TwoDigitNumberField(field);
            default:
                return new PaddedNumberField(field, padding);
        }
    }

    // Format methods
    //-----------------------------------------------------------------------

    public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos) {
        if (obj instanceof Date) {
            return format((Date) obj, toAppendTo);
        } else if (obj instanceof Calendar) {
            return format((Calendar) obj, toAppendTo);
        } else if (obj instanceof Long) {
            return format(((Long) obj).longValue(), toAppendTo);
        } else {
            throw new IllegalArgumentException("Unknown class: " +
                    (obj == null ? "<null>" : obj.getClass().getName()));
        }
    }

    public String format(final long millis) {
        final Calendar c = newCalendar();  // hard code GregorianCalendar
        c.setTimeInMillis(millis);
        return applyRulesToString(c);
    }

    
    private String applyRulesToString(final Calendar c) {
        return applyRules(c, new StringBuffer(mMaxLengthEstimate)).toString();
    }

    
    private GregorianCalendar newCalendar() {
        // hard code GregorianCalendar
        return new GregorianCalendar(mTimeZone, mLocale);
    }

    public String format(final Date date) {
        final Calendar c = newCalendar();  // hard code GregorianCalendar
        c.setTime(date);
        return applyRulesToString(c);
    }

    public String format(final Calendar calendar) {
        return format(calendar, new StringBuffer(mMaxLengthEstimate)).toString();
    }

    public StringBuffer format(final long millis, final StringBuffer buf) {
        return format(new Date(millis), buf);
    }

    public StringBuffer format(final Date date, final StringBuffer buf) {
        final Calendar c = newCalendar();  // hard code GregorianCalendar
        c.setTime(date);
        return applyRules(c, buf);
    }

    public StringBuffer format(final Calendar calendar, final StringBuffer buf) {
        return applyRules(calendar, buf);
    }

    
    protected StringBuffer applyRules(final Calendar calendar, final StringBuffer buf) {
        for (final Rule rule : mRules) {
            rule.appendTo(buf, calendar);
        }
        return buf;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof FastDatePrinter == false) {
            return false;
        }
        final FastDatePrinter other = (FastDatePrinter) obj;
        return mPattern.equals(other.mPattern)
                && mTimeZone.equals(other.mTimeZone)
                && mLocale.equals(other.mLocale);
    }

    
    @Override
    public int hashCode() {
        return mPattern.hashCode() + 13 * (mTimeZone.hashCode() + 13 * mLocale.hashCode());
    }

    
    @Override
    public String toString() {
        return "FastDatePrinter[" + mPattern + "," + mLocale + "," + mTimeZone.getID() + "]";
    }

    private interface Rule {
        
        int estimateLength();

        
        void appendTo(StringBuffer buffer, Calendar calendar);
    }

    
    private interface NumberRule extends Rule {
        
        void appendTo(StringBuffer buffer, int value);
    }

    
    private static class CharacterLiteral implements Rule {
        private final char mValue;

        
        CharacterLiteral(final char value) {
            mValue = value;
        }

        
        @Override
        public int estimateLength() {
            return 1;
        }

        
        @Override
        public void appendTo(final StringBuffer buffer, final Calendar calendar) {
            buffer.append(mValue);
        }
    }

    
    private static class StringLiteral implements Rule {
        private final String mValue;

        
        StringLiteral(final String value) {
            mValue = value;
        }

        
        @Override
        public int estimateLength() {
            return mValue.length();
        }

        
        @Override
        public void appendTo(final StringBuffer buffer, final Calendar calendar) {
            buffer.append(mValue);
        }
    }

    
    private static class TextField implements Rule {
        private final int mField;
        private final String[] mValues;

        
        TextField(final int field, final String[] values) {
            mField = field;
            mValues = values;
        }

        
        @Override
        public int estimateLength() {
            int max = 0;
            for (int i=mValues.length; --i >= 0; ) {
                final int len = mValues[i].length();
                if (len > max) {
                    max = len;
                }
            }
            return max;
        }

        
        @Override
        public void appendTo(final StringBuffer buffer, final Calendar calendar) {
            buffer.append(mValues[calendar.get(mField)]);
        }
    }

    
    private static class UnpaddedNumberField implements NumberRule {
        private final int mField;

        
        UnpaddedNumberField(final int field) {
            mField = field;
        }

        
        @Override
        public int estimateLength() {
            return 4;
        }

        
        @Override
        public void appendTo(final StringBuffer buffer, final Calendar calendar) {
            appendTo(buffer, calendar.get(mField));
        }

        
        @Override
        public final void appendTo(final StringBuffer buffer, final int value) {
            if (value < 10) {
                buffer.append((char)(value + '0'));
            } else if (value < 100) {
                buffer.append((char)(value / 10 + '0'));
                buffer.append((char)(value % 10 + '0'));
            } else {
                buffer.append(Integer.toString(value));
            }
        }
    }

    
    private static class UnpaddedMonthField implements NumberRule {
        static final UnpaddedMonthField INSTANCE = new UnpaddedMonthField();

        
        UnpaddedMonthField() {
            super();
        }

        
        @Override
        public int estimateLength() {
            return 2;
        }

        
        @Override
        public void appendTo(final StringBuffer buffer, final Calendar calendar) {
            appendTo(buffer, calendar.get(Calendar.MONTH) + 1);
        }

        
        @Override
        public final void appendTo(final StringBuffer buffer, final int value) {
            if (value < 10) {
                buffer.append((char)(value + '0'));
            } else {
                buffer.append((char)(value / 10 + '0'));
                buffer.append((char)(value % 10 + '0'));
            }
        }
    }

    
    private static class PaddedNumberField implements NumberRule {
        private final int mField;
        private final int mSize;

        
        PaddedNumberField(final int field, final int size) {
            if (size < 3) {
                // Should use UnpaddedNumberField or TwoDigitNumberField.
                throw new IllegalArgumentException();
            }
            mField = field;
            mSize = size;
        }

        
        @Override
        public int estimateLength() {
            return 4;
        }

        
        @Override
        public void appendTo(final StringBuffer buffer, final Calendar calendar) {
            appendTo(buffer, calendar.get(mField));
        }

        
        @Override
        public final void appendTo(final StringBuffer buffer, final int value) {
            if (value < 100) {
                for (int i = mSize; --i >= 2; ) {
                    buffer.append('0');
                }
                buffer.append((char)(value / 10 + '0'));
                buffer.append((char)(value % 10 + '0'));
            } else {
                int digits;
                if (value < 1000) {
                    digits = 3;
                } else {
                    Assert.isTrue(value > -1, "Negative values should not be possible");
                    digits = Integer.toString(value).length();
                }
                for (int i = mSize; --i >= digits; ) {
                    buffer.append('0');
                }
                buffer.append(Integer.toString(value));
            }
        }
    }

    
    private static class TwoDigitNumberField implements NumberRule {
        private final int mField;

        
        TwoDigitNumberField(final int field) {
            mField = field;
        }

        
        @Override
        public int estimateLength() {
            return 2;
        }

        
        @Override
        public void appendTo(final StringBuffer buffer, final Calendar calendar) {
            appendTo(buffer, calendar.get(mField));
        }

        
        @Override
        public final void appendTo(final StringBuffer buffer, final int value) {
            if (value < 100) {
                buffer.append((char)(value / 10 + '0'));
                buffer.append((char)(value % 10 + '0'));
            } else {
                buffer.append(Integer.toString(value));
            }
        }
    }

    
    private static class TwoDigitYearField implements NumberRule {
        static final TwoDigitYearField INSTANCE = new TwoDigitYearField();

        
        TwoDigitYearField() {
            super();
        }

        
        @Override
        public int estimateLength() {
            return 2;
        }

        
        @Override
        public void appendTo(final StringBuffer buffer, final Calendar calendar) {
            appendTo(buffer, calendar.get(Calendar.YEAR) % 100);
        }

        
        @Override
        public final void appendTo(final StringBuffer buffer, final int value) {
            buffer.append((char)(value / 10 + '0'));
            buffer.append((char)(value % 10 + '0'));
        }
    }

    
    private static class TwoDigitMonthField implements NumberRule {
        static final TwoDigitMonthField INSTANCE = new TwoDigitMonthField();

        
        TwoDigitMonthField() {
            super();
        }

        
        @Override
        public int estimateLength() {
            return 2;
        }

        
        @Override
        public void appendTo(final StringBuffer buffer, final Calendar calendar) {
            appendTo(buffer, calendar.get(Calendar.MONTH) + 1);
        }

        
        @Override
        public final void appendTo(final StringBuffer buffer, final int value) {
            buffer.append((char)(value / 10 + '0'));
            buffer.append((char)(value % 10 + '0'));
        }
    }

    
    private static class TwelveHourField implements NumberRule {
        private final NumberRule mRule;

        
        TwelveHourField(final NumberRule rule) {
            mRule = rule;
        }

        
        @Override
        public int estimateLength() {
            return mRule.estimateLength();
        }

        
        @Override
        public void appendTo(final StringBuffer buffer, final Calendar calendar) {
            int value = calendar.get(Calendar.HOUR);
            if (value == 0) {
                value = calendar.getLeastMaximum(Calendar.HOUR) + 1;
            }
            mRule.appendTo(buffer, value);
        }

        
        @Override
        public void appendTo(final StringBuffer buffer, final int value) {
            mRule.appendTo(buffer, value);
        }
    }

    
    private static class TwentyFourHourField implements NumberRule {
        private final NumberRule mRule;

        
        TwentyFourHourField(final NumberRule rule) {
            mRule = rule;
        }

        
        @Override
        public int estimateLength() {
            return mRule.estimateLength();
        }

        
        @Override
        public void appendTo(final StringBuffer buffer, final Calendar calendar) {
            int value = calendar.get(Calendar.HOUR_OF_DAY);
            if (value == 0) {
                value = calendar.getMaximum(Calendar.HOUR_OF_DAY) + 1;
            }
            mRule.appendTo(buffer, value);
        }

        @Override
        public void appendTo(final StringBuffer buffer, final int value) {
            mRule.appendTo(buffer, value);
        }
    }

    private static final ConcurrentMap<TimeZoneDisplayKey, String> cTimeZoneDisplayCache =
            new ConcurrentHashMap<TimeZoneDisplayKey, String>(7);
    
    static String getTimeZoneDisplay(final TimeZone tz, final boolean daylight, final int style, final Locale locale) {
        final TimeZoneDisplayKey key = new TimeZoneDisplayKey(tz, daylight, style, locale);
        String value = cTimeZoneDisplayCache.get(key);
        if (value == null) {
            // This is a very slow call, so cache the results.
            value = tz.getDisplayName(daylight, style, locale);
            final String prior = cTimeZoneDisplayCache.putIfAbsent(key, value);
            if (prior != null) {
                value= prior;
            }
        }
        return value;
    }

    
    private static class TimeZoneNameRule implements Rule {
        private final Locale mLocale;
        private final int mStyle;
        private final String mStandard;
        private final String mDaylight;

        
        TimeZoneNameRule(final TimeZone timeZone, final Locale locale, final int style) {
            mLocale = locale;
            mStyle = style;

            mStandard = getTimeZoneDisplay(timeZone, false, style, locale);
            mDaylight = getTimeZoneDisplay(timeZone, true, style, locale);
        }

        
        @Override
        public int estimateLength() {
            // We have no access to the Calendar object that will be passed to
            // appendTo so base estimate on the TimeZone passed to the
            // constructor
            return Math.max(mStandard.length(), mDaylight.length());
        }

        
        @Override
        public void appendTo(final StringBuffer buffer, final Calendar calendar) {
            final TimeZone zone = calendar.getTimeZone();
            if (zone.useDaylightTime()
                    && calendar.get(Calendar.DST_OFFSET) != 0) {
                buffer.append(getTimeZoneDisplay(zone, true, mStyle, mLocale));
            } else {
                buffer.append(getTimeZoneDisplay(zone, false, mStyle, mLocale));
            }
        }
    }

    
    private static class TimeZoneNumberRule implements Rule {
        static final TimeZoneNumberRule INSTANCE_COLON = new TimeZoneNumberRule(true);
        static final TimeZoneNumberRule INSTANCE_NO_COLON = new TimeZoneNumberRule(false);

        final boolean mColon;

        
        TimeZoneNumberRule(final boolean colon) {
            mColon = colon;
        }

        
        @Override
        public int estimateLength() {
            return 5;
        }

        
        @Override
        public void appendTo(final StringBuffer buffer, final Calendar calendar) {
            int offset = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);

            if (offset < 0) {
                buffer.append('-');
                offset = -offset;
            } else {
                buffer.append('+');
            }

            final int hours = offset / (60 * 60 * 1000);
            buffer.append((char)(hours / 10 + '0'));
            buffer.append((char)(hours % 10 + '0'));

            if (mColon) {
                buffer.append(':');
            }

            final int minutes = offset / (60 * 1000) - 60 * hours;
            buffer.append((char)(minutes / 10 + '0'));
            buffer.append((char)(minutes % 10 + '0'));
        }
    }

    private static class TimeZoneDisplayKey {
        private final TimeZone mTimeZone;
        private final int mStyle;
        private final Locale mLocale;

        
        TimeZoneDisplayKey(final TimeZone timeZone,
                           final boolean daylight, final int style, final Locale locale) {
            mTimeZone = timeZone;
            if (daylight) {
                mStyle = style | 0x80000000;
            } else {
                mStyle = style;
            }
            mLocale = locale;
        }

        
        @Override
        public int hashCode() {
            return (mStyle * 31 + mLocale.hashCode() ) * 31 + mTimeZone.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof TimeZoneDisplayKey) {
                final TimeZoneDisplayKey other = (TimeZoneDisplayKey)obj;
                return
                        mTimeZone.equals(other.mTimeZone) &&
                                mStyle == other.mStyle &&
                                mLocale.equals(other.mLocale);
            }
            return false;
        }
    }
}
