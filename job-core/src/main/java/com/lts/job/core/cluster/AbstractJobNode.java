package com.lts.job.core.cluster;

import com.lts.job.core.Application;
import com.lts.job.core.constant.EcTopic;
import com.lts.job.core.domain.JobNodeConfig;
import com.lts.job.core.factory.JobNodeConfigFactory;
import com.lts.job.core.factory.NodeFactory;
import com.lts.job.core.listener.MasterChangeListener;
import com.lts.job.core.listener.MasterElectionListener;
import com.lts.job.core.listener.NodeChangeListener;
import com.lts.job.core.listener.SelfChangeListener;
import com.lts.job.core.protocol.command.CommandBodyWrapper;
import com.lts.job.core.registry.*;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.core.util.GenericsUtils;
import com.lts.job.ec.EventInfo;
import com.lts.job.ec.EventSubscriber;
import com.lts.job.ec.JvmEventCenter;
import com.lts.job.ec.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/15/14.
 *         抽象节点
 */
public abstract class AbstractJobNode<T extends Node, App extends Application> implements JobNode {

    protected static final Logger LOGGER = LoggerFactory.getLogger(JobNode.class);

    protected Registry registry;
    protected T node;
    protected JobNodeConfig config;
    protected App application;
    private List<NodeChangeListener> nodeChangeListeners;

    public AbstractJobNode() {
        application = getApplication();
        config = JobNodeConfigFactory.getDefaultConfig();
        application.setConfig(config);
        // 事件中心
        application.setEventCenter(new JvmEventCenter());
        application.setCommandBodyWrapper(new CommandBodyWrapper(application));
        application.setMasterElector(new MasterElector(application));
        nodeChangeListeners = new ArrayList<NodeChangeListener>();
    }

    final public void start() {
        try {
            node = NodeFactory.create(getNodeClass(), config);
            config.setNodeType(node.getNodeType());

            initRegistry();

            LOGGER.info("当前节点配置:{}", config);

            // 监听节点 启用/禁用消息
            application.getEventCenter().subscribe(
                    new String[]{EcTopic.NODE_DISABLE, EcTopic.NODE_ENABLE},
                    new EventSubscriber(node.getIdentity(), new Observer() {
                        @Override
                        public void onObserved(EventInfo eventInfo) {
                            if (EcTopic.NODE_DISABLE.equals(eventInfo.getTopic())) {
                                nodeDisable();
                            } else {
                                nodeEnable();
                            }
                        }
                    }));

            innerStart();

            registry.register(node);

            LOGGER.info("启动成功!");

        } catch (Throwable e) {
            LOGGER.error("启动失败!", e);
        }
    }

    final public void stop() {
        try {
            registry.unregister(node);

            innerStop();

            LOGGER.info("停止成功!");
        } catch (Throwable e) {
            LOGGER.error("停止失败!", e);
        }
    }

    @Override
    public void destroy() {
        try {
            registry.destroy();
        } catch (Throwable e) {
            LOGGER.error("销毁失败!", e);
        }
    }

    private void initRegistry() {
        registry = RegistryFactory.getRegistry(application);
        ((AbstractRegistry) registry).setNode(node);
        // 订阅的node管理
        SubscribedNodeManager subscribedNodeManager = new SubscribedNodeManager(application);
        application.setSubscribedNodeManager(subscribedNodeManager);
        nodeChangeListeners.add(subscribedNodeManager);
        // 用于master选举的监听器
        nodeChangeListeners.add(new MasterElectionListener(application));
        // 监听自己节点变化（如，当前节点被禁用了）
        nodeChangeListeners.add(new SelfChangeListener(application));

        registry.subscribe(node, new NotifyListener() {
            private final Logger NOTIFY_LOGGER = LoggerFactory.getLogger(NotifyListener.class);

            @Override
            public void notify(NotifyEvent event, List<Node> nodes) {
                if (CollectionUtils.isEmpty(nodes)) {
                    return;
                }
                switch (event) {
                    case ADD:
                        for (NodeChangeListener listener : nodeChangeListeners) {
                            try {
                                listener.addNodes(nodes);
                            } catch (Throwable t) {
                                NOTIFY_LOGGER.error("{} add nodes failed , cause: {}", listener.getClass(), t.getMessage(), t);
                            }
                        }
                        break;
                    case REMOVE:
                        for (NodeChangeListener listener : nodeChangeListeners) {
                            try {
                                listener.removeNodes(nodes);
                            } catch (Throwable t) {
                                NOTIFY_LOGGER.error("{} remove nodes failed , cause: {}", listener.getClass(), t.getMessage(), t);
                            }
                        }
                        break;
                }
            }
        });
    }

    protected abstract void innerStart();

    protected abstract void innerStop();

    protected abstract void nodeEnable();

    protected abstract void nodeDisable();

    @SuppressWarnings("unchecked")
    private App getApplication() {
        try {
            return ((Class<App>)
                    GenericsUtils.getSuperClassGenericType(this.getClass(), 1))
                    .newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<T> getNodeClass() {
        return (Class<T>)
                GenericsUtils.getSuperClassGenericType(this.getClass(), 0);
    }


    /**
     * 设置zookeeper注册中心地址
     *
     * @param registryAddress
     */
    public void setRegistryAddress(String registryAddress) {
        config.setRegistryAddress(registryAddress);
    }

    /**
     * 设置远程调用超时时间
     *
     * @param invokeTimeoutMillis
     */
    public void setInvokeTimeoutMillis(int invokeTimeoutMillis) {
        config.setInvokeTimeoutMillis(invokeTimeoutMillis);
    }

    /**
     * 设置集群名字
     *
     * @param clusterName
     */
    public void setClusterName(String clusterName) {
        config.setClusterName(clusterName);
    }

    /**
     * 添加节点监听器
     *
     * @param notifyListener
     */
    public void addNodeChangeListener(NodeChangeListener notifyListener) {
        if (notifyListener != null) {
            nodeChangeListeners.add(notifyListener);
        }
    }

    /**
     * 添加 master 节点变化监听器
     *
     * @param masterChangeListener
     */
    public void addMasterChangeListener(MasterChangeListener masterChangeListener) {
        application.getMasterElector().addMasterChangeListener(masterChangeListener);
    }

    /**
     * 设置额外的配置参数
     *
     * @param key
     * @param value
     */
    public void addConfig(String key, String value) {
        config.setParameter(key, value);
    }
}
