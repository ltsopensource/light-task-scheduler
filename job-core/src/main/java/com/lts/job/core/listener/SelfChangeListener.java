package com.lts.job.core.listener;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.constant.EcTopic;
import com.lts.job.core.extension.ExtensionLoader;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.ec.EventCenter;
import com.lts.job.ec.EventCenterFactory;
import com.lts.job.ec.EventInfo;

import java.util.List;

/**
 * 用来监听自己的节点信息变化
 *
 * @author Robert HG (254963746@qq.com) on 5/11/15.
 */
public class SelfChangeListener implements NodeChangeListener {

    private Config config;
    private EventCenter eventCenter;
    private EventCenterFactory eventCenterFactory = ExtensionLoader.getExtensionLoader(EventCenterFactory.class).getAdaptiveExtension();

    public SelfChangeListener(Config config) {
        this.config = config;
        this.eventCenter = eventCenterFactory.getEventCenter(config);
    }


    private void change(Node node) {
        if (node.getIdentity().equals(config.getIdentity())) {
            // 是当前节点, 看看节点配置是否发生变化
            // 1. 看 threads 有没有改变 , 目前只有 TASK_TRACKER 对 threads起作用
            if (node.getNodeType().equals(NodeType.TASK_TRACKER)
                    && (node.getThreads() != config.getWorkThreads())) {
                config.setWorkThreads(node.getThreads());
                eventCenter.publishAsync(new EventInfo(EcTopic.WORK_THREAD_CHANGE));
            }

            // 2. 看 available 有没有改变
            if (node.isAvailable() != config.isAvailable()) {
                String topic = node.isAvailable() ? EcTopic.NODE_ENABLE : EcTopic.NODE_DISABLE;
                config.setAvailable(node.isAvailable());
                eventCenter.publishAsync(new EventInfo(topic));
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
