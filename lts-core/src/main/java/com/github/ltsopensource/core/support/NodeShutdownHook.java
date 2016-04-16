package com.github.ltsopensource.core.support;

import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.commons.utils.Callable;
import com.github.ltsopensource.core.constant.EcTopic;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.ec.EventInfo;
import com.github.ltsopensource.ec.EventSubscriber;
import com.github.ltsopensource.ec.Observer;

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
