package com.lts.tasktracker.processor;

import com.lts.remoting.RemotingProcessor;
import com.lts.tasktracker.domain.TaskTrackerApplication;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 */
public abstract class AbstractProcessor implements RemotingProcessor {

    protected TaskTrackerApplication application;

    protected AbstractProcessor(TaskTrackerApplication application) {
        this.application = application;
    }
}
