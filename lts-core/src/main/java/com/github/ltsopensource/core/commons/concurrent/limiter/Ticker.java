package com.github.ltsopensource.core.commons.concurrent.limiter;

/**
 * A time source; returns a time value representing the number of nanoseconds elapsed since some
 * fixed but arbitrary point in time. Note that most users should use {@link com.google.common.base.Stopwatch} instead of
 * interacting with this class directly.
 * <p/>
 * <p><b>Warning:</b> this interface can only be used to measure elapsed time, not wall time.
 *
 * @author Kevin Bourrillion
 * @since 10.0
 * (<a href="https://github.com/google/guava/wiki/Compatibility"
 * >mostly source-compatible</a> since 9.0)
 */
public abstract class Ticker {
    /**
     * Constructor for use by subclasses.
     */
    protected Ticker() {
    }

    /**
     * Returns the number of nanoseconds elapsed since this ticker's fixed
     * point of reference.
     */
    public abstract long read();

    /**
     * A ticker that reads the current time using {@link System#nanoTime}.
     *
     * @since 10.0
     */
    public static Ticker systemTicker() {
        return SYSTEM_TICKER;
    }

    private static final Ticker SYSTEM_TICKER =
            new Ticker() {
                @Override
                public long read() {
                    return System.nanoTime();
                }
            };
}
