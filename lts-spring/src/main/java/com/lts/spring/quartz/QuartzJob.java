package com.lts.spring.quartz;

import org.quartz.Trigger;
import org.springframework.util.MethodInvoker;

/**
 * @author Robert HG (254963746@qq.com) on 3/16/16.
 */
class QuartzJob {

    private String name;

    private QuartzJobType type;

    private Trigger trigger;

    private MethodInvoker methodInvoker;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MethodInvoker getMethodInvoker() {
        return methodInvoker;
    }

    public void setMethodInvoker(MethodInvoker methodInvoker) {
        this.methodInvoker = methodInvoker;
    }

    public QuartzJobType getType() {
        return type;
    }

    public void setType(QuartzJobType type) {
        this.type = type;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }
}
