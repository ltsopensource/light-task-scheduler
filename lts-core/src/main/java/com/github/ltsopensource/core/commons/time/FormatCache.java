package com.github.ltsopensource.core.commons.time;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * from commons-lang 为了性能
 */
abstract class FormatCache<F extends Format> {

    private final ConcurrentMap<MultipartKey, F> cInstanceCache = new ConcurrentHashMap<MultipartKey, F>(7);

    private static final ConcurrentMap<MultipartKey, String> cDateTimeInstanceCache = new ConcurrentHashMap<MultipartKey, String>(7);

    public F getInstance() {
        return getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, TimeZone.getDefault(), Locale.getDefault());
    }

    public F getInstance(final String pattern, TimeZone timeZone, Locale locale) {
        if (pattern == null) {
            throw new NullPointerException("pattern must not be null");
        }
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        final MultipartKey key = new MultipartKey(pattern, timeZone, locale);
        F format = cInstanceCache.get(key);
        if (format == null) {
            format = createInstance(pattern, timeZone, locale);
            final F previousValue = cInstanceCache.putIfAbsent(key, format);
            if (previousValue != null) {
                // another thread snuck in and did the same work
                // we should return the instance that is in ConcurrentMap
                format = previousValue;
            }
        }
        return format;
    }

    abstract protected F createInstance(String pattern, TimeZone timeZone, Locale locale);

    private F getDateTimeInstance(final Integer dateStyle, final Integer timeStyle, final TimeZone timeZone, Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        final String pattern = getPatternForStyle(dateStyle, timeStyle, locale);
        return getInstance(pattern, timeZone, locale);
    }

    F getDateTimeInstance(final int dateStyle, final int timeStyle, final TimeZone timeZone, Locale locale) {
        return getDateTimeInstance(Integer.valueOf(dateStyle), Integer.valueOf(timeStyle), timeZone, locale);
    }

    F getDateInstance(final int dateStyle, final TimeZone timeZone, Locale locale) {
        return getDateTimeInstance(dateStyle, null, timeZone, locale);
    }

    F getTimeInstance(final int timeStyle, final TimeZone timeZone, Locale locale) {
        return getDateTimeInstance(null, timeStyle, timeZone, locale);
    }

    static String getPatternForStyle(final Integer dateStyle, final Integer timeStyle, final Locale locale) {
        final MultipartKey key = new MultipartKey(dateStyle, timeStyle, locale);

        String pattern = cDateTimeInstanceCache.get(key);
        if (pattern == null) {
            try {
                DateFormat formatter;
                if (dateStyle == null) {
                    formatter = DateFormat.getTimeInstance(timeStyle, locale);
                } else if (timeStyle == null) {
                    formatter = DateFormat.getDateInstance(dateStyle, locale);
                } else {
                    formatter = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
                }
                pattern = ((SimpleDateFormat) formatter).toPattern();
                final String previous = cDateTimeInstanceCache.putIfAbsent(key, pattern);
                if (previous != null) {
                    // even though it doesn't matter if another thread put the pattern
                    // it's still good practice to return the String instance that is
                    // actually in the ConcurrentMap
                    pattern = previous;
                }
            } catch (final ClassCastException ex) {
                throw new IllegalArgumentException("No date time pattern for locale: " + locale);
            }
        }
        return pattern;
    }

    private static class MultipartKey {
        private final Object[] keys;
        private int hashCode;


        public MultipartKey(final Object... keys) {
            this.keys = keys;
        }

        @Override
        public boolean equals(final Object obj) {
            return Arrays.equals(keys, ((MultipartKey) obj).keys);
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                int rc = 0;
                for (final Object key : keys) {
                    if (key != null) {
                        rc = rc * 7 + key.hashCode();
                    }
                }
                hashCode = rc;
            }
            return hashCode;
        }
    }

}

