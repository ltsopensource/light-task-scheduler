package com.lts.job.ec.injvm;

import com.lts.job.core.cluster.Config;
import com.lts.job.ec.EventCenter;
import com.lts.job.ec.EventCenterFactory;

/**
 * @author Robert HG (254963746@qq.com) on 5/19/15.
 */
public class InJvmEventCenterFactory implements EventCenterFactory {

    @Override
    public EventCenter getEventCenter(Config config) {
        return new InjvmEventCenter();
    }
}
