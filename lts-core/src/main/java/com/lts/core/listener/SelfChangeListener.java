package com.lts.core.listener;

import com.lts.core.Application;
import com.lts.core.cluster.Config;
import com.lts.core.cluster.Node;
import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.constant.EcTopic;
import com.lts.ec.EventInfo;

import java.util.List;

/**
 * 用来监听自己的节点信息变化
 *
 * @author Robert HG (254963746@qq.com) on 5/11/15.
 */
public class SelfChangeListener implements NodeChangeListener {

    private Config config;
    private Application application;

    public SelfChangeListener(Application application) {
        this.config = application.getConfig();
        this.application = application;
    }


    private void change(Node node) {
        if (node.getIdentity().equals(config.getIdentity())) {
            // 是当前节点, 看看节点配置是否发生变化
            // 1. 看 threads 有没有改变 , 目前只有 TASK_TRACKER 对 threads起作用
            if (node.getNodeType().equals(NodeType.TASK_TRACKER)
                    && (node.getThreads() != config.getWorkThreads())) {
                config.setWorkThreads(node.getThreads());
                application.getEventCenter().publishAsync(new EventInfo(EcTopic.WORK_THREAD_CHANGE));
            }

            // 2. 看 available 有没有改变
            if (node.isAvailable() != config.isAvailable()) {
                String topic = node.isAvailable() ? EcTopic.NODE_ENABLE : EcTopic.NODE_DISABLE;
                config.setAvailable(node.isAvailable());
                application.getEventCenter().publishAsync(new EventInfo(topic));
            }
        }
    }

    @Override
    public void addNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            change(node);
        }
    }

    @Override
    public void removeNodes(List<Node> nodes) {

    }
}
