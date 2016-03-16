package com.lts.spring.quartz;

import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.util.MethodInvoker;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/16/16.
 */
class QuartzSchedulerBeanTargetEditor extends PropertyEditorSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzSchedulerBeanTargetEditor.class);
    private QuartzProxyContext context;

    public QuartzSchedulerBeanTargetEditor(QuartzProxyContext context) {
        this.context = context;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void setValue(Object value) {
        if (value != null && value instanceof Collection) {

            Collection<Trigger> triggers = (Collection<Trigger>) value;
            List<QuartzCronJob> quartzCronJobs = new ArrayList<QuartzCronJob>(triggers.size());
            for (Trigger trigger : triggers) {
                if (trigger instanceof CronTriggerImpl) {
                    quartzCronJobs.add(buildQuartzCronJob((CronTriggerImpl) trigger));
                } else {
                    LOGGER.error("Can't Proxy " + trigger.getClass().getName());
                }
            }
            context.getAgent().startProxy(quartzCronJobs);
        }
        super.setValue(new ArrayList<Trigger>(0));
    }

    private QuartzCronJob buildQuartzCronJob(CronTriggerImpl cronTrigger) {
        JobDataMap jobDataMap = cronTrigger.getJobDataMap();
        JobDetail jobDetail = (JobDetail) jobDataMap.get("jobDetail");

        // 要执行的类
        MethodInvoker methodInvoker = (MethodInvoker) jobDetail.getJobDataMap().get("methodInvoker");

        QuartzCronJob quartzCronJob = new QuartzCronJob();
        quartzCronJob.setCronTrigger(cronTrigger);
        quartzCronJob.setMethodInvoker(methodInvoker);

        return quartzCronJob;
    }

}
