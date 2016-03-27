package com.lts.spring.quartz.invoke;

import com.lts.core.domain.Job;
import com.lts.spring.quartz.QuartzJobContext;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/16.
 */
public interface JobExecution {

    public void execute(QuartzJobContext quartzJobContext, Job job) throws Throwable;

}
