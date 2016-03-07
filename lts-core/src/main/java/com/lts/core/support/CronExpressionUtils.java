package com.lts.core.support;

import com.lts.core.exception.CronException;
import com.lts.core.support.CronExpression;

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

    public static boolean isValidExpression(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

}
