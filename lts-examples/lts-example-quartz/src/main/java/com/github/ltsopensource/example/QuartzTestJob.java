package com.github.ltsopensource.example;

import com.github.ltsopensource.core.commons.utils.DateUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 3/16/16.
 */
public class QuartzTestJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        for (Map.Entry<String, Object> entry : context.getMergedJobDataMap().entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
        System.out.println(DateUtils.formatYMD_HMS(new Date()) + " 我开始执行了...");
    }
}
