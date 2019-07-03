package com.github.ltsopensource.core;

import com.github.ltsopensource.core.support.CronExpression;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Robert HG (254963746@qq.com) on 3/3/15.
 */
public class CronExpressionTest {

    @Ignore
    @Test
    public void test1() throws ParseException {

        CronExpression cronExpression = new CronExpression("59 23 * * *");

        exec(cronExpression, new Date());
    }

    private Date exec(CronExpression cronExpression, Date date){

        Date nextDate = cronExpression.getTimeAfter(date);

        if(nextDate != null){
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nextDate));

            nextDate.setTime(nextDate.getTime() + 100);
            exec(cronExpression, nextDate);
        }else{
            System.out.println("执行完成");
        }

        return nextDate;
    }


}
