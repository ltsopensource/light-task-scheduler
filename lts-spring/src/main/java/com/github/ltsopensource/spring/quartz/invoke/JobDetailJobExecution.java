package com.github.ltsopensource.spring.quartz.invoke;

import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.spring.quartz.QuartzJobContext;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.core.jmx.JobDataMapSupport;

import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/16.
 */
public class JobDetailJobExecution implements JobExecution {

    private org.quartz.Job quartzJob;

    public JobDetailJobExecution(org.quartz.Job quartzJob) {
        this.quartzJob = quartzJob;
    }

    @Override
    public void execute(QuartzJobContext quartzJobContext, Job job) throws Throwable {

        JobDataMap jobDataMap = JobDataMapSupport.newJobDataMap(quartzJobContext.getJobDataMap());

        // 用lts的覆盖
        Map<String, String> map = job.getExtParams();
        if (CollectionUtils.isNotEmpty(map)) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                jobDataMap.put(entry.getKey(), entry.getValue());
            }
        }

        JobExecutionContext jobExecutionContext = new JobExecutionContextImpl(jobDataMap);
        quartzJob.execute(jobExecutionContext);
    }
}
