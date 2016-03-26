package com.lts.spring.quartz;

import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;
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

        List<Object> nativeQuartzTriggers = new ArrayList<Object>();

        if (value != null && value instanceof Collection) {

            Collection<Trigger> triggers = (Collection<Trigger>) value;
            List<QuartzJob> quartzJobs = new ArrayList<QuartzJob>(triggers.size());
            for (Trigger trigger : triggers) {
                if (trigger instanceof CronTriggerImpl) {
                    quartzJobs.add(buildQuartzCronJob((CronTriggerImpl) trigger));
                } else if (trigger instanceof SimpleTriggerImpl) {
                    quartzJobs.add(buildQuartzSimpleJob((SimpleTriggerImpl) trigger));
                } else {
                    LOGGER.warn("Can't Proxy " + trigger.getClass().getName() + " Then Use Quartz Scheduler");
                    nativeQuartzTriggers.add(trigger);
                }
            }
            context.getAgent().startProxy(quartzJobs);
        }
        super.setValue(nativeQuartzTriggers);
    }

    private QuartzJob buildQuartzCronJob(CronTriggerImpl cronTrigger) {
        JobDataMap jobDataMap = cronTrigger.getJobDataMap();
        JobDetail jobDetail = (JobDetail) jobDataMap.get("jobDetail");

        // 要执行的类
        MethodInvoker methodInvoker = (MethodInvoker) jobDetail.getJobDataMap().get("methodInvoker");

        QuartzJob quartzJob = new QuartzJob();
        quartzJob.setTrigger(cronTrigger);
        quartzJob.setType(QuartzJobType.CRON);
        quartzJob.setName(cronTrigger.getName());
        quartzJob.setMethodInvoker(methodInvoker);

        return quartzJob;
    }

    private QuartzJob buildQuartzSimpleJob(SimpleTriggerImpl simpleTrigger) {
        JobDataMap jobDataMap = simpleTrigger.getJobDataMap();
        JobDetail jobDetail = (JobDetail) jobDataMap.get("jobDetail");

        // 要执行的类
        MethodInvoker methodInvoker = (MethodInvoker) jobDetail.getJobDataMap().get("methodInvoker");
        QuartzJob quartzJob = new QuartzJob();
        quartzJob.setTrigger(simpleTrigger);
        quartzJob.setName(simpleTrigger.getName());
        quartzJob.setType(QuartzJobType.SIMPLE_REPEAT);
        quartzJob.setMethodInvoker(methodInvoker);

        return quartzJob;
    }
}
