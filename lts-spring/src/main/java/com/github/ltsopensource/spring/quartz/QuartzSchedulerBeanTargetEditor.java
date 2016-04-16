package com.github.ltsopensource.spring.quartz;

import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.spring.quartz.invoke.JobDetailJobExecution;
import com.github.ltsopensource.spring.quartz.invoke.MethodInvokeJobExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.springframework.util.MethodInvoker;

import java.beans.PropertyEditorSupport;
import java.util.*;

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
            List<QuartzJobContext> quartzJobContexts = new ArrayList<QuartzJobContext>(triggers.size());
            for (Trigger trigger : triggers) {
                if (trigger instanceof CronTriggerImpl) {
                    quartzJobContexts.add(buildQuartzCronJob((CronTriggerImpl) trigger));
                } else if (trigger instanceof SimpleTriggerImpl) {
                    quartzJobContexts.add(buildQuartzSimpleJob((SimpleTriggerImpl) trigger));
                } else {
                    LOGGER.warn("Can't Proxy " + trigger.getClass().getName() + " Then Use Quartz Scheduler");
                    nativeQuartzTriggers.add(trigger);
                }
            }
            context.getAgent().startProxy(quartzJobContexts);
        }
        super.setValue(nativeQuartzTriggers);
    }

    private QuartzJobContext buildQuartzCronJob(CronTriggerImpl cronTrigger) {
        QuartzJobContext quartzJobContext = new QuartzJobContext();
        quartzJobContext.setTrigger(cronTrigger);
        quartzJobContext.setType(QuartzJobType.CRON);
        quartzJobContext.setName(cronTrigger.getName());

        buildQuartzJobContext(quartzJobContext, cronTrigger);

        return quartzJobContext;
    }

    private QuartzJobContext buildQuartzSimpleJob(SimpleTriggerImpl simpleTrigger) {
        QuartzJobContext quartzJobContext = new QuartzJobContext();
        quartzJobContext.setTrigger(simpleTrigger);
        quartzJobContext.setName(simpleTrigger.getName());
        quartzJobContext.setType(QuartzJobType.SIMPLE_REPEAT);

        buildQuartzJobContext(quartzJobContext, simpleTrigger);

        return quartzJobContext;
    }

    private QuartzJobContext buildQuartzJobContext(QuartzJobContext quartzJobContext, Trigger trigger) {
        JobDataMap triggerJobDataMap = trigger.getJobDataMap();
        JobDetail jobDetail = (JobDetail) triggerJobDataMap.get("jobDetail");
        // 要执行的类
        MethodInvoker methodInvoker = (MethodInvoker) jobDetail.getJobDataMap().get("methodInvoker");
        Map<String, Object> jobDataMap = new HashMap<String, Object>();
        jobDataMap.putAll(triggerJobDataMap);
        jobDataMap.putAll(jobDetail.getJobDataMap());
        jobDataMap.remove("jobDetail");
        jobDataMap.remove("methodInvoker");

        quartzJobContext.setJobDataMap(jobDataMap);
        if (methodInvoker != null) {
            quartzJobContext.setJobExecution(new MethodInvokeJobExecution(methodInvoker));
        } else {
            Class<? extends Job> jobClass = jobDetail.getJobClass();
            try {
                Job job = jobClass.newInstance();
                quartzJobContext.setJobExecution(new JobDetailJobExecution(job));
            } catch (Exception e) {
                throw new QuartzProxyException("Instance JobClass[" + (jobClass == null ? null : jobClass.getName()) + "] error", e);
            }
        }
        return quartzJobContext;
    }
}
