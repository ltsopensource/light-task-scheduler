package com.github.ltsopensource.core.support;

import com.github.ltsopensource.core.exception.CronException;

import java.text.ParseException;
import java.util.Date;

/**
 * @author Robert HG (254963746@qq.com) on 5/27/15.
 */
public class CronExpressionUtils {

    private CronExpressionUtils() {
    }

    public static Date getNextTriggerTime(String cronExpression) {
        try {
            CronExpression cron = new CronExpression(cronExpression);
            return cron.getTimeAfter(new Date());
        } catch (ParseException e) {
            throw new CronException(e);
        }
    }

    public static Date getNextTriggerTime(String cronExpression, Date timeAfter) {
        try {
            CronExpression cron = new CronExpression(cronExpression);
            if (timeAfter == null) {
                timeAfter = new Date();
            }
            return cron.getTimeAfter(timeAfter);
        } catch (ParseException e) {
            throw new CronException(e);
        }
    }

    public static boolean isValidExpression(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

}
