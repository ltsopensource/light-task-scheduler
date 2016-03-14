package com.lts.core.support;

import com.lts.core.AppContext;
import com.lts.core.commons.utils.Callable;
import com.lts.core.constant.EcTopic;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.ec.EventInfo;
import com.lts.ec.EventSubscriber;
import com.lts.ec.Observer;

/**
 * @author Robert HG (254963746@qq.com) on 3/14/16.
 */
public class NodeShutdownHook {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeShutdownHook.class);

    public static void registerHook(AppContext appContext, final String name, final Callable callback) {
        appContext.getEventCenter().subscribe(new EventSubscriber(name + "_" + appContext.getConfig().getIdentity(), new Observer() {
            @Override
            public void onObserved(EventInfo eventInfo) {
                if (callback != null) {
                    try {
                        callback.call();
                    } catch (Exception e) {
                        LOGGER.warn("Call shutdown hook {} error", name, e);
                    }
                }
            }
        }), EcTopic.NODE_SHUT_DOWN);
    }

}
