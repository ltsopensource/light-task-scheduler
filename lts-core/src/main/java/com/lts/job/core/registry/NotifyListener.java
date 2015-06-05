package com.lts.job.core.registry;

import com.lts.job.core.cluster.Node;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/17/15.
 */
public interface NotifyListener {

    void notify(NotifyEvent event, List<Node> nodes);

}
