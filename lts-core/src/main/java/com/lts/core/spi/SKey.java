package com.lts.core.spi;

/**
 * Created by hugui.hg on 12/27/15.
 */
public interface SKey {

    String FAIL_STORE = "job.fail.store";

    String LOADBALANCE = "loadbalance";

    String EVENT_CENTER = "event.center";

    String REMOTING = "lts.remoting";

    String REMOTING_SERIALIZABLE_DFT = "lts.remoting.serializable.default";

    String ZK_CLIENT_KEY = "zk.client";

    String JOB_ID_GENERATOR = "id.generator";

    String JOB_LOGGER = "job.logger";

    String JOB_QUEUE = "job.queue";
}
