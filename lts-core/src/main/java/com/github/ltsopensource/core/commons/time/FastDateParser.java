package com.github.ltsopensource.core.commons.time;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * from commons-lang 为了性能
 */
public class FastDateParser {

    static final Locale JAPANESE_IMPERIAL = new Locale("ja", "JP", "JP");

    // defining fields
    private final String pattern;
    private final TimeZone timeZone;
    private final Locale locale;
    private final int century;
    private final int startYear;

    // derived fields
    private transient Pattern parsePattern;
    private transient Strategy[] strategies;

    // dynamic fields to communicate with Strategy
    private transient String currentFormatField;
    private transient Strategy nextStrategy;

    protected FastDateParser(final String pattern, final TimeZone timeZone, final Locale locale) {
        this(pattern, timeZone, locale, null);
    }

    protected FastDateParser(final String pattern, final TimeZone timeZone, final Locale locale, final Date centuryStart) {
        this.pattern = pattern;
        this.timeZone = timeZone;
        this.locale = locale;

        final Calendar definingCalendar = Calendar.getInstance(timeZone, locale);
        int centuryStartYear;
        if (centuryStart != null) {
            definingCalendar.setTime(centuryStart);
            centuryStartYear = definingCalendar.get(Calendar.YEAR);
        } else if (locale.equals(JAPANESE_IMPERIAL)) {
            centuryStartYear = 0;
        } else {
            // from 80 years ago to 20 years from now
            definingCalendar.setTime(new Date());
            centuryStartYear = definingCalendar.get(Calendar.YEAR) - 80;
        }
        century = centuryStartYear / 100 * 100;
        startYear = centuryStartYear - century;

        init(definingCalendar);
    }

    private void init(Calendar definingCalendar) {

        final StringBuilder regex = new StringBuilder();
        final List<Strategy> collector = new ArrayList<Strategy>();

        final Matcher patternMatcher = formatPattern.matcher(pattern);
        if (!patternMatcher.lookingAt()) {
            throw new IllegalArgumentException(
                    "Illegal pattern character '" + pattern.charAt(patternMatcher.regionStart()) + "'");
        }

        currentFormatField = patternMatcher.group();
        Strategy currentStrategy = getStrategy(currentFormatField, definingCalendar);
        for (; ; ) {
            patternMatcher.region(patternMatcher.end(), patternMatcher.regionEnd());
            if (!patternMatcher.lookingAt()) {
                nextStrategy = null;
                break;
            }
            final String nextFormatField = patternMatcher.group();
            nextStrategy = getStrategy(nextFormatField, definingCalendar);
            if (currentStrategy.addRegex(this, regex)) {
                collector.add(currentStrategy);
            }
            currentFormatField = nextFormatField;
            currentStrategy = nextStrategy;
        }
        if (patternMatcher.regionStart() != patternMatcher.regionEnd()) {
            throw new IllegalArgumentException("Failed to parse \"" + pattern + "\" ; gave up at index " + patternMatcher.regionStart());
        }
        if (currentStrategy.addRegex(this, regex)) {
            collector.add(currentStrategy);
        }
        currentFormatField = null;
        strategies = collector.toArray(new Strategy[collector.size()]);
        parsePattern = Pattern.compile(regex.toString());
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof FastDateParser)) {
            return false;
        }
        final FastDateParser other = (FastDateParser) obj;
        return pattern.equals(other.pattern)
                && timeZone.equals(other.timeZone)
                && locale.equals(other.locale);
    }


    @Override
    public int hashCode() {
        return pattern.hashCode() + 13 * (timeZone.hashCode() + 13 * locale.hashCode());
    }


    @Override
    public String toString() {
        return "FastDateParser[" + pattern + "," + locale + "," + timeZone.getID() + "]";
    }

    public Object parseObject(final String source) throws ParseException {
        return parse(source);
    }

    public Date parse(final String source) throws ParseException {
        final Date date = parse(source, new ParsePosition(0));
        if (date == null) {
            // Add a note re supported date range
            if (locale.equals(JAPANESE_IMPERIAL)) {
                throw new ParseException(
                        "(The " + locale + " locale does not support dates before 1868 AD)\n" +
                                "Unparseable date: \"" + source + "\" does not match " + parsePattern.pattern(), 0);
            }
            throw new ParseException("Unparseable date: \"" + source + "\" does not match " + parsePattern.pattern(), 0);
        }
        return date;
    }

    public Object parseObject(final String source, final ParsePosition pos) {
        return parse(source, pos);
    }

    public Date parse(final String source, final ParsePosition pos) {
        final int offset = pos.getIndex();
        final Matcher matcher = parsePattern.matcher(source.substring(offset));
        if (!matcher.lookingAt()) {
            return null;
        }
        // timing tests indicate getting new instance is 19% faster than cloning
        final Calendar cal = Calendar.getInstance(timeZone, locale);
        cal.clear();

        for (int i = 0; i < strategies.length; ) {
            final Strategy strategy = strategies[i++];
            strategy.setCalendar(this, cal, matcher.group(i));
        }
        pos.setIndex(offset + matcher.end());
        return cal.getTime();
    }

    // Support for strategies
    //-----------------------------------------------------------------------
    private static StringBuilder escapeRegex(final StringBuilder regex, final String value, final boolean unquote) {
        regex.append("\\Q");
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            switch (c) {
                case '\'':
                    if (unquote) {
                        if (++i == value.length()) {
                            return regex;
                        }
                        c = value.charAt(i);
                    }
                    break;
                case '\\':
                    if (++i == value.length()) {
                        break;
                    }

                    regex.append(c); // we always want the original \
                    c = value.charAt(i); // Is it followed by E ?
                    if (c == 'E') { // \E detected
                        regex.append("E\\\\E\\"); // see comment above
                        c = 'Q'; // appended below
                    }
                    break;
                default:
                    break;
            }
            regex.append(c);
        }
        regex.append("\\E");
        return regex;
    }


    private static Map<String, Integer> getDisplayNames(final int field, final Calendar definingCalendar, final Locale locale) {
        return definingCalendar.getDisplayNames(field, Calendar.ALL_STYLES, locale);
    }


    private int adjustYear(final int twoDigitYear) {
        int trial = century + twoDigitYear;
        return twoDigitYear >= startYear ? trial : trial + 100;
    }


    boolean isNextNumber() {
        return nextStrategy != null && nextStrategy.isNumber();
    }


    int getFieldWidth() {
        return currentFormatField.length();
    }


    private static abstract class Strategy {

        boolean isNumber() {
            return false;
        }

        void setCalendar(final FastDateParser parser, final Calendar cal, final String value) {

        }

        abstract boolean addRegex(FastDateParser parser, StringBuilder regex);
    }


    private static final Pattern formatPattern = Pattern.compile(
            "D+|E+|F+|G+|H+|K+|M+|S+|W+|Z+|a+|d+|h+|k+|m+|s+|w+|y+|z+|''|'[^']++(''[^']*+)*+'|[^'A-Za-z]++");


    private Strategy getStrategy(final String formatField, final Calendar definingCalendar) {
        switch (formatField.charAt(0)) {
            case '\'':
                if (formatField.length() > 2) {
                    return new CopyQuotedStrategy(formatField.substring(1, formatField.length() - 1));
                }
                //$FALL-THROUGH$
            default:
                return new CopyQuotedStrategy(formatField);
            case 'D':
                return DAY_OF_YEAR_STRATEGY;
            case 'E':
                return getLocaleSpecificStrategy(Calendar.DAY_OF_WEEK, definingCalendar);
            case 'F':
                return DAY_OF_WEEK_IN_MONTH_STRATEGY;
            case 'G':
                return getLocaleSpecificStrategy(Calendar.ERA, definingCalendar);
            case 'H':
                return MODULO_HOUR_OF_DAY_STRATEGY;
            case 'K':
                return HOUR_STRATEGY;
            case 'M':
                return formatField.length() >= 3 ? getLocaleSpecificStrategy(Calendar.MONTH, definingCalendar) : NUMBER_MONTH_STRATEGY;
            case 'S':
                return MILLISECOND_STRATEGY;
            case 'W':
                return WEEK_OF_MONTH_STRATEGY;
            case 'a':
                return getLocaleSpecificStrategy(Calendar.AM_PM, definingCalendar);
            case 'd':
                return DAY_OF_MONTH_STRATEGY;
            case 'h':
                return MODULO_HOUR_STRATEGY;
            case 'k':
                return HOUR_OF_DAY_STRATEGY;
            case 'm':
                return MINUTE_STRATEGY;
            case 's':
                return SECOND_STRATEGY;
            case 'w':
                return WEEK_OF_YEAR_STRATEGY;
            case 'y':
                return formatField.length() > 2 ? LITERAL_YEAR_STRATEGY : ABBREVIATED_YEAR_STRATEGY;
            case 'Z':
            case 'z':
                return getLocaleSpecificStrategy(Calendar.ZONE_OFFSET, definingCalendar);
        }
    }

    @SuppressWarnings("unchecked") // OK because we are creating an array with no entries
    private static final ConcurrentMap<Locale, Strategy>[] caches = new ConcurrentMap[Calendar.FIELD_COUNT];


    private static ConcurrentMap<Locale, Strategy> getCache(final int field) {
        synchronized (caches) {
            if (caches[field] == null) {
                caches[field] = new ConcurrentHashMap<Locale, Strategy>(3);
            }
            return caches[field];
        }
    }


    private Strategy getLocaleSpecificStrategy(final int field, final Calendar definingCalendar) {
        final ConcurrentMap<Locale, Strategy> cache = getCache(field);
        Strategy strategy = cache.get(locale);
        if (strategy == null) {
            strategy = field == Calendar.ZONE_OFFSET
                    ? new TimeZoneStrategy(locale)
                    : new TextStrategy(field, definingCalendar, locale);
            final Strategy inCache = cache.putIfAbsent(locale, strategy);
            if (inCache != null) {
                return inCache;
            }
        }
        return strategy;
    }


    private static class CopyQuotedStrategy extends Strategy {
        private final String formatField;


        CopyQuotedStrategy(final String formatField) {
            this.formatField = formatField;
        }


        @Override
        boolean isNumber() {
            char c = formatField.charAt(0);
            if (c == '\'') {
                c = formatField.charAt(1);
            }
            return Character.isDigit(c);
        }


        @Override
        boolean addRegex(final FastDateParser parser, final StringBuilder regex) {
            escapeRegex(regex, formatField, true);
            return false;
        }
    }


    private static class TextStrategy extends Strategy {
        private final int field;
        private final Map<String, Integer> keyValues;


        TextStrategy(final int field, final Calendar definingCalendar, final Locale locale) {
            this.field = field;
            this.keyValues = getDisplayNames(field, definingCalendar, locale);
        }


        @Override
        boolean addRegex(final FastDateParser parser, final StringBuilder regex) {
            regex.append('(');
            for (final String textKeyValue : keyValues.keySet()) {
                escapeRegex(regex, textKeyValue, false).append('|');
            }
            regex.setCharAt(regex.length() - 1, ')');
            return true;
        }


        @Override
        void setCalendar(final FastDateParser parser, final Calendar cal, final String value) {
            final Integer iVal = keyValues.get(value);
            if (iVal == null) {
                final StringBuilder sb = new StringBuilder(value);
                sb.append(" not in (");
                for (final String textKeyValue : keyValues.keySet()) {
                    sb.append(textKeyValue).append(' ');
                }
                sb.setCharAt(sb.length() - 1, ')');
                throw new IllegalArgumentException(sb.toString());
            }
            cal.set(field, iVal.intValue());
        }
    }

    private static class NumberStrategy extends Strategy {
        private final int field;


        NumberStrategy(final int field) {
            this.field = field;
        }


        @Override
        boolean isNumber() {
            return true;
        }


        @Override
        boolean addRegex(final FastDateParser parser, final StringBuilder regex) {
            // See LANG-954: We use {Nd} rather than {IsNd} because Android does not support the Is prefix
            if (parser.isNextNumber()) {
                regex.append("(\\p{Nd}{").append(parser.getFieldWidth()).append("}+)");
            } else {
                regex.append("(\\p{Nd}++)");
            }
            return true;
        }


        @Override
        void setCalendar(final FastDateParser parser, final Calendar cal, final String value) {
            cal.set(field, modify(Integer.parseInt(value)));
        }


        int modify(final int iValue) {
            return iValue;
        }
    }

    private static final Strategy ABBREVIATED_YEAR_STRATEGY = new NumberStrategy(Calendar.YEAR) {

        @Override
        void setCalendar(final FastDateParser parser, final Calendar cal, final String value) {
            int iValue = Integer.parseInt(value);
            if (iValue < 100) {
                iValue = parser.adjustYear(iValue);
            }
            cal.set(Calendar.YEAR, iValue);
        }
    };


    private static class TimeZoneStrategy extends Strategy {

        private final String validTimeZoneChars;
        private final SortedMap<String, TimeZone> tzNames = new TreeMap<String, TimeZone>(String.CASE_INSENSITIVE_ORDER);


        private static final int ID = 0;

        private static final int LONG_STD = 1;

        private static final int SHORT_STD = 2;

        private static final int LONG_DST = 3;

        private static final int SHORT_DST = 4;


        TimeZoneStrategy(final Locale locale) {
            final String[][] zones = DateFormatSymbols.getInstance(locale).getZoneStrings();
            for (String[] zone : zones) {
                if (zone[ID].startsWith("GMT")) {
                    continue;
                }
                final TimeZone tz = TimeZone.getTimeZone(zone[ID]);
                if (!tzNames.containsKey(zone[LONG_STD])) {
                    tzNames.put(zone[LONG_STD], tz);
                }
                if (!tzNames.containsKey(zone[SHORT_STD])) {
                    tzNames.put(zone[SHORT_STD], tz);
                }
                if (tz.useDaylightTime()) {
                    if (!tzNames.containsKey(zone[LONG_DST])) {
                        tzNames.put(zone[LONG_DST], tz);
                    }
                    if (!tzNames.containsKey(zone[SHORT_DST])) {
                        tzNames.put(zone[SHORT_DST], tz);
                    }
                }
            }

            final StringBuilder sb = new StringBuilder();
            sb.append("(GMT[+\\-]\\d{0,1}\\d{2}|[+\\-]\\d{2}:?\\d{2}|");
            for (final String id : tzNames.keySet()) {
                escapeRegex(sb, id, false).append('|');
            }
            sb.setCharAt(sb.length() - 1, ')');
            validTimeZoneChars = sb.toString();
        }


        @Override
        boolean addRegex(final FastDateParser parser, final StringBuilder regex) {
            regex.append(validTimeZoneChars);
            return true;
        }


        @Override
        void setCalendar(final FastDateParser parser, final Calendar cal, final String value) {
            TimeZone tz;
            if (value.charAt(0) == '+' || value.charAt(0) == '-') {
                tz = TimeZone.getTimeZone("GMT" + value);
            } else if (value.startsWith("GMT")) {
                tz = TimeZone.getTimeZone(value);
            } else {
                tz = tzNames.get(value);
                if (tz == null) {
                    throw new IllegalArgumentException(value + " is not a supported timezone name");
                }
            }
            cal.setTimeZone(tz);
        }
    }

    private static final Strategy NUMBER_MONTH_STRATEGY = new NumberStrategy(Calendar.MONTH) {
        @Override
        int modify(final int iValue) {
            return iValue - 1;
        }
    };
    private static final Strategy LITERAL_YEAR_STRATEGY = new NumberStrategy(Calendar.YEAR);
    private static final Strategy WEEK_OF_YEAR_STRATEGY = new NumberStrategy(Calendar.WEEK_OF_YEAR);
    private static final Strategy WEEK_OF_MONTH_STRATEGY = new NumberStrategy(Calendar.WEEK_OF_MONTH);
    private static final Strategy DAY_OF_YEAR_STRATEGY = new NumberStrategy(Calendar.DAY_OF_YEAR);
    private static final Strategy DAY_OF_MONTH_STRATEGY = new NumberStrategy(Calendar.DAY_OF_MONTH);
    private static final Strategy DAY_OF_WEEK_IN_MONTH_STRATEGY = new NumberStrategy(Calendar.DAY_OF_WEEK_IN_MONTH);
    private static final Strategy HOUR_OF_DAY_STRATEGY = new NumberStrategy(Calendar.HOUR_OF_DAY);
    private static final Strategy MODULO_HOUR_OF_DAY_STRATEGY = new NumberStrategy(Calendar.HOUR_OF_DAY) {
        @Override
        int modify(final int iValue) {
            return iValue % 24;
        }
    };
    private static final Strategy MODULO_HOUR_STRATEGY = new NumberStrategy(Calendar.HOUR) {
        @Override
        int modify(final int iValue) {
            return iValue % 12;
        }
    };
    private static final Strategy HOUR_STRATEGY = new NumberStrategy(Calendar.HOUR);
    private static final Strategy MINUTE_STRATEGY = new NumberStrategy(Calendar.MINUTE);
    private static final Strategy SECOND_STRATEGY = new NumberStrategy(Calendar.SECOND);
    private static final Strategy MILLISECOND_STRATEGY = new NumberStrategy(Calendar.MILLISECOND);
}
