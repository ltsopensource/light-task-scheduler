package com.lts.ec.injvm;

import com.lts.core.cluster.Config;
import com.lts.ec.EventCenter;
import com.lts.ec.EventCenterFactory;

/**
 * @author Robert HG (254963746@qq.com) on 5/19/15.
 */
public class InJvmEventCenterFactory implements EventCenterFactory {

    @Override
    public EventCenter getEventCenter(Config config) {
        return new InjvmEventCenter();
    }
}
