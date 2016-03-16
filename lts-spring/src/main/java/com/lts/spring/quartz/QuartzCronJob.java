package com.lts.spring.quartz;

import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.util.MethodInvoker;

/**
 * @author Robert HG (254963746@qq.com) on 3/16/16.
 */
class QuartzCronJob {

    private CronTriggerImpl cronTrigger;

    private MethodInvoker methodInvoker;

    public CronTriggerImpl getCronTrigger() {
        return cronTrigger;
    }

    public void setCronTrigger(CronTriggerImpl cronTrigger) {
        this.cronTrigger = cronTrigger;
    }

    public MethodInvoker getMethodInvoker() {
        return methodInvoker;
    }

    public void setMethodInvoker(MethodInvoker methodInvoker) {
        this.methodInvoker = methodInvoker;
    }
}
