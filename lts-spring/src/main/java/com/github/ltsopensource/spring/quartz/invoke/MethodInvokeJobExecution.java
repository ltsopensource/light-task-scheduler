package com.github.ltsopensource.spring.quartz.invoke;

import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.spring.quartz.QuartzJobContext;
import org.springframework.util.MethodInvoker;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/16.
 */
public class MethodInvokeJobExecution implements JobExecution {

    private MethodInvoker methodInvoker;

    public MethodInvokeJobExecution(MethodInvoker methodInvoker) {
        this.methodInvoker = methodInvoker;
    }

    @Override
    public void execute(QuartzJobContext quartzJobContext, Job job) throws Throwable {
        methodInvoker.invoke();
    }
}
