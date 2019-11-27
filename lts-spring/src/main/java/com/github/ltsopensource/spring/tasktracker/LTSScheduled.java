package com.github.ltsopensource.spring.tasktracker;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 可以被LTS解析的 org.springframework.scheduling.annotation.Scheduled
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface LTSScheduled {
	/**
	 * Sharding Key / ID
	 */
	String value();
	/**
	 * A cron-like expression, extending the usual UN*X definition to include
	 * triggers on the second as well as minute, hour, day of month, month
	 * and day of week.  e.g. {@code "0 * * * * MON-FRI"} means once per minute on
	 * weekdays (at the top of the minute - the 0th second).
	 * @return an expression that can be parsed to a cron schedule
	 * @see org.springframework.scheduling.support.CronSequenceGenerator
	 */
	String cron() default "";

	/**
	 * Execute the annotated method with a fixed period between the end
	 * of the last invocation and the start of the next.
	 * @return the delay in milliseconds
	 */
	long fixedDelay() default -1;

	/**
	 * Execute the annotated method with a fixed period between the end
	 * of the last invocation and the start of the next.
	 * @return the delay in milliseconds as a String value, e.g. a placeholder
	 * @since 3.2.2
	 */
	String fixedDelayString() default "";

	/**
	 * Execute the annotated method with a fixed period between invocations.
	 * @return the period in milliseconds
	 */
	long fixedRate() default -1;

	/**
	 * Execute the annotated method with a fixed period between invocations.
	 * @return the period in milliseconds as a String value, e.g. a placeholder
	 * @since 3.2.2
	 */
	String fixedRateString() default "";

	/**
	 * Number of milliseconds to delay before the first execution of a
	 * {@link #fixedRate()} or {@link #fixedDelay()} task.
	 * @return the initial delay in milliseconds
	 * @since 3.2
	 */
	long initialDelay() default -1;

	/**
	 * Number of milliseconds to delay before the first execution of a
	 * {@link #fixedRate()} or {@link #fixedDelay()} task.
	 * @return the initial delay in milliseconds as a String value, e.g. a placeholder
	 * @since 3.2.2
	 */
	String initialDelayString() default "";
}
