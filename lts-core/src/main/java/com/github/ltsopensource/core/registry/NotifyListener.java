package com.github.ltsopensource.core.registry;

import com.github.ltsopensource.core.cluster.Node;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/17/15.
 */
public interface NotifyListener {

    void notify(NotifyEvent event, List<Node> nodes);

}
