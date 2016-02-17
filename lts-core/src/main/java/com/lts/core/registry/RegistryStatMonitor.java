package com.lts.core.registry;

import com.lts.core.AppContext;
import com.lts.core.constant.EcTopic;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.ec.EventInfo;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 9/8/15.
 */
public class RegistryStatMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryStatMonitor.class);
    private AppContext appContext;
    private AtomicBoolean available = new AtomicBoolean(false);

    public RegistryStatMonitor(AppContext appContext) {
        this.appContext = appContext;
    }

    public void setAvailable(boolean available) {
        this.available.set(available);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Registry {}", available ? "available" : "unavailable");
        }
        // 发布事件
        appContext.getEventCenter().publishAsync(new EventInfo(
                available ? EcTopic.REGISTRY_AVAILABLE : EcTopic.REGISTRY_UN_AVAILABLE));
    }

    public boolean isAvailable() {
        return this.available.get();
    }

}
