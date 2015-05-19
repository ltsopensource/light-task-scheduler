package com.lts.job.core.registry.multcast;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.cluster.Node;
import com.lts.job.core.registry.FailbackRegistry;
import com.lts.job.core.registry.NotifyListener;

/**
 * @author Robert HG (254963746@qq.com) on 5/19/15.
 */
public class MulticastRegistry extends FailbackRegistry {

    public MulticastRegistry(Config config) {
        super(config);
    }

    @Override
    protected void doRegister(Node node) {

    }

    @Override
    protected void doUnRegister(Node node) {

    }

    @Override
    protected void doSubscribe(Node node, NotifyListener listener) {

    }

    @Override
    protected void doUnsubscribe(Node node, NotifyListener listener) {

    }

}
