package com.github.ltsopensource.core.cluster;

import com.github.ltsopensource.cmd.HttpCmdServer;
import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.cmd.JVMInfoGetHttpCmd;
import com.github.ltsopensource.core.cmd.StatusCheckHttpCmd;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.GenericsUtils;
import com.github.ltsopensource.core.commons.utils.NetUtils;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.compiler.AbstractCompiler;
import com.github.ltsopensource.core.constant.EcTopic;
import com.github.ltsopensource.core.constant.ExtConfig;
import com.github.ltsopensource.core.factory.JobNodeConfigFactory;
import com.github.ltsopensource.core.factory.NodeFactory;
import com.github.ltsopensource.core.json.JSONFactory;
import com.github.ltsopensource.core.listener.MasterChangeListener;
import com.github.ltsopensource.core.listener.MasterElectionListener;
import com.github.ltsopensource.core.listener.NodeChangeListener;
import com.github.ltsopensource.core.listener.SelfChangeListener;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.protocol.command.CommandBodyWrapper;
import com.github.ltsopensource.core.registry.*;
import com.github.ltsopensource.core.spi.ServiceLoader;
import com.github.ltsopensource.core.support.AliveKeeping;
import com.github.ltsopensource.core.support.ConfigValidator;
import com.github.ltsopensource.ec.EventCenter;
import com.github.ltsopensource.ec.EventInfo;
import com.github.ltsopensource.remoting.serialize.AdaptiveSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 8/15/14.
 *         抽象节点
 */
public abstract class AbstractJobNode<T extends Node, Context extends AppContext> implements JobNode {

    protected static final Logger LOGGER = LoggerFactory.getLogger(JobNode.class);

    protected Registry registry;
    protected T node;
    protected Config config;
    protected Context appContext;
    private List<NodeChangeListener> nodeChangeListeners;
    private List<MasterChangeListener> masterChangeListeners;
    protected AtomicBoolean started = new AtomicBoolean(false);

    public AbstractJobNode() {
        appContext = getAppContext();
        node = NodeFactory.create(getNodeClass());
        config = JobNodeConfigFactory.getDefaultConfig();
        config.setNodeType(node.getNodeType());
        appContext.setConfig(config);
        nodeChangeListeners = new ArrayList<NodeChangeListener>();
        masterChangeListeners = new ArrayList<MasterChangeListener>();
    }

    final public void start() {
        try {
            if (started.compareAndSet(false, true)) {

                configValidate();

                // 初始化配置
                initConfig();

                // 初始化HttpCmdServer
                initHttpCmdServer();

                beforeRemotingStart();

                remotingStart();

                afterRemotingStart();

                initRegistry();

                registry.register(node);

                AliveKeeping.start();

                LOGGER.info("========== Start success, nodeType={}, identity={}", config.getNodeType(), config.getIdentity());
            }
        } catch (Throwable e) {
            if (e.getMessage().contains("Address already in use")) {
                LOGGER.error("========== Start failed at listen port {}, nodeType={}, identity={}", config.getListenPort(), config.getNodeType(), config.getIdentity(), e);
            } else {
                LOGGER.error("========== Start failed, nodeType={}, identity={}", config.getNodeType(), config.getIdentity(), e);
            }
        }
    }

    private void initHttpCmdServer() {
        // 命令中心
        int port = appContext.getConfig().getParameter(ExtConfig.HTTP_CMD_PORT, 8719);
        appContext.setHttpCmdServer(HttpCmdServer.Factory.getHttpCmdServer(config.getIp(), port));

        // 先启动，中间看端口是否被占用
        appContext.getHttpCmdServer().start();
        // 设置command端口，会暴露到注册中心上
        node.setHttpCmdPort(appContext.getHttpCmdServer().getPort());

        appContext.getHttpCmdServer().registerCommands(
                new StatusCheckHttpCmd(appContext.getConfig()),
                new JVMInfoGetHttpCmd(appContext.getConfig()));        // 状态检查
    }

    final public void stop() {
        try {
            if (started.compareAndSet(true, false)) {

                if (registry != null) {
                    registry.unregister(node);
                }

                beforeRemotingStop();

                remotingStop();

                afterRemotingStop();

                appContext.getEventCenter().publishSync(new EventInfo(EcTopic.NODE_SHUT_DOWN));

                AliveKeeping.stop();

                LOGGER.info("========== Stop success, nodeType={}, identity={}", config.getNodeType(), config.getIdentity());
            }
        } catch (Throwable e) {
            LOGGER.error("========== Stop failed, nodeType={}, identity={}", config.getNodeType(), config.getIdentity(), e);
        }
    }

    @Override
    public void destroy() {
        try {
            registry.destroy();
            LOGGER.info("Destroy success, nodeType={}, identity={}", config.getNodeType(), config.getIdentity());
        } catch (Throwable e) {
            LOGGER.error("Destroy failed, nodeType={}, identity={}", config.getNodeType(), config.getIdentity(), e);
        }
    }

    protected void configValidate() {
        ConfigValidator.validateNodeGroup(config.getNodeGroup());
        ConfigValidator.validateClusterName(config.getClusterName());
        ConfigValidator.validateIdentity(config.getIdentity());
    }

    protected void initConfig() {

        String compiler = config.getParameter(ExtConfig.COMPILER);
        if (StringUtils.isNotEmpty(compiler)) {
            AbstractCompiler.setCompiler(compiler);
        }

        if (StringUtils.isEmpty(config.getIp())) {
            config.setIp(NetUtils.getLocalHost());
        }
        if (StringUtils.isEmpty(config.getIdentity())) {
            JobNodeConfigFactory.buildIdentity(config);
        }
        NodeFactory.build(node, config);

        LOGGER.info("Current Node config :{}", config);

        appContext.setEventCenter(ServiceLoader.load(EventCenter.class, config));

        appContext.setCommandBodyWrapper(new CommandBodyWrapper(config));
        appContext.setMasterElector(new MasterElector(appContext));
        appContext.getMasterElector().addMasterChangeListener(masterChangeListeners);
        appContext.setRegistryStatMonitor(new RegistryStatMonitor(appContext));

        // 订阅的node管理
        SubscribedNodeManager subscribedNodeManager = new SubscribedNodeManager(appContext);
        appContext.setSubscribedNodeManager(subscribedNodeManager);
        nodeChangeListeners.add(subscribedNodeManager);
        // 用于master选举的监听器
        nodeChangeListeners.add(new MasterElectionListener(appContext));
        // 监听自己节点变化（如，当前节点被禁用了）
        nodeChangeListeners.add(new SelfChangeListener(appContext));

        setSpiConfig();
    }

    private void setSpiConfig() {
        // 设置默认序列化方式
        String defaultSerializable = config.getParameter(ExtConfig.REMOTING_SERIALIZABLE_DFT);
        if (StringUtils.isNotEmpty(defaultSerializable)) {
            AdaptiveSerializable.setDefaultSerializable(defaultSerializable);
        }

        // 设置json
        String ltsJson = config.getParameter(ExtConfig.LTS_JSON);
        if (StringUtils.isNotEmpty(ltsJson)) {
            JSONFactory.setJSONAdapter(ltsJson);
        }

        // 设置logger
        String logger = config.getParameter(ExtConfig.LTS_LOGGER);
        if (StringUtils.isNotEmpty(logger)) {
            LoggerFactory.setLoggerAdapter(logger);
        }
    }

    private void initRegistry() {
        registry = RegistryFactory.getRegistry(appContext);
        if (registry instanceof AbstractRegistry) {
            ((AbstractRegistry) registry).setNode(node);
        }
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
                                NOTIFY_LOGGER.error("{} add nodes failed , cause: {}", listener.getClass().getName(), t.getMessage(), t);
                            }
                        }
                        break;
                    case REMOVE:
                        for (NodeChangeListener listener : nodeChangeListeners) {
                            try {
                                listener.removeNodes(nodes);
                            } catch (Throwable t) {
                                NOTIFY_LOGGER.error("{} remove nodes failed , cause: {}", listener.getClass().getName(), t.getMessage(), t);
                            }
                        }
                        break;
                }
            }
        });
    }

    protected abstract void remotingStart();

    protected abstract void remotingStop();

    protected abstract void beforeRemotingStart();

    protected abstract void afterRemotingStart();

    protected abstract void beforeRemotingStop();

    protected abstract void afterRemotingStop();

    @SuppressWarnings("unchecked")
    private Context getAppContext() {
        try {
            return ((Class<Context>)
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
     */
    public void setRegistryAddress(String registryAddress) {
        config.setRegistryAddress(registryAddress);
    }

    /**
     * 设置远程调用超时时间
     */
    public void setInvokeTimeoutMillis(int invokeTimeoutMillis) {
        config.setInvokeTimeoutMillis(invokeTimeoutMillis);
    }

    /**
     * 设置集群名字
     */
    public void setClusterName(String clusterName) {
        config.setClusterName(clusterName);
    }

    /**
     * 节点标识(必须要保证这个标识是唯一的才能设置，请谨慎设置)
     * 这个是非必须设置的，建议使用系统默认生成
     */
    public void setIdentity(String identity) {
        config.setIdentity(identity);
    }

    /**
     * 添加节点监听器
     */
    public void addNodeChangeListener(NodeChangeListener notifyListener) {
        if (notifyListener != null) {
            nodeChangeListeners.add(notifyListener);
        }
    }

    /**
     * 显示设置绑定ip
     */
    public void setBindIp(String bindIp) {
        if (StringUtils.isEmpty(bindIp)
                || !NetUtils.isValidHost(bindIp)
                ) {
            throw new IllegalArgumentException("Invalided bind ip:" + bindIp);
        }
        config.setIp(bindIp);
    }

    /**
     * 添加 master 节点变化监听器
     */
    public void addMasterChangeListener(MasterChangeListener masterChangeListener) {
        if (masterChangeListener != null) {
            masterChangeListeners.add(masterChangeListener);
        }
    }

    public void setDataPath(String path) {
        if (StringUtils.isNotEmpty(path)) {
            config.setDataPath(path);
        }
    }

    /**
     * 设置额外的配置参数
     */
    public void addConfig(String key, String value) {
        config.setParameter(key, value);
    }
}
