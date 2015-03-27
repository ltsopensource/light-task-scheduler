package com.lts.job.core.cluster;

import com.lts.job.core.constant.Constants;
import com.lts.job.core.domain.JobNodeConfig;
import com.lts.job.core.listener.MasterNodeChangeListener;
import com.lts.job.core.listener.NodeChangeListener;
import com.lts.job.core.protocol.command.CommandBodyWrapper;
import com.lts.job.core.registry.Registry;
import com.lts.job.core.registry.ZkNodeRegistry;
import com.lts.job.core.Application;
import com.lts.job.core.listener.MasterNodeElectionListener;
import com.lts.job.core.util.GenericsUtils;
import com.lts.job.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert HG (254963746@qq.com) on 8/15/14.
 * 抽象节点
 */
public abstract class AbstractJobNode<T extends Node> implements JobNode {

    protected static final Logger LOGGER = LoggerFactory.getLogger(JobNode.class);

    protected Registry registry;
    protected T node;
    protected JobNodeConfig config;
    protected Application application;

    public AbstractJobNode() {

        config = new JobNodeConfig();
        config.setIdentity(StringUtils.generateUUID());
        config.setWorkThreads(Constants.AVAILABLE_PROCESSOR);
        config.setNodeGroup("lts");
        config.setZookeeperAddress("localhost:2181");
        config.setInvokeTimeoutMillis(1000 * 6);
        config.setListenPort(0);
        config.setJobInfoSavePath(Constants.USER_HOME);
        config.setClusterName(Constants.DEFAULT_CLUSTER_NAME);
        // 可用的线程数
        application = new Application();
        application.setAttribute(Constants.KEY_AVAILABLE_THREADS, config.getWorkThreads());

        application.setConfig(config);
        application.setCommandBodyWrapper(new CommandBodyWrapper(application));
        application.setNodeManager(new NodeManager(application));
        application.setMasterElector(new MasterElector(application));

        this.registry = new ZkNodeRegistry(application);
        // 用于master选举的监听器
        addNodeChangeListener(new MasterNodeElectionListener(application));
    }

    final public void start() {
        try {
            Class<T> nodeClass = GenericsUtils.getSuperClassGenericType(this.getClass());
            node = NodeFactory.create(application.getPathParser(), nodeClass, config);
            config.setNodeType(node.getNodeType());

            LOGGER.info("当前节点配置:{}", config);

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

    protected abstract void innerStart();

    protected abstract void innerStop();

    /**
     * 设置zookeeper注册中心地址
     * @param zookeeperAddress
     */
    public void setZookeeperAddress(String zookeeperAddress) {
        config.setZookeeperAddress(zookeeperAddress);
    }

    /**
     * 设置远程调用超时时间
     * @param invokeTimeoutMillis
     */
    public void setInvokeTimeoutMillis(int invokeTimeoutMillis) {
        config.setInvokeTimeoutMillis(invokeTimeoutMillis);
    }

    /**
     * 设置集群名字
     * @param clusterName
     */
    public void setClusterName(String clusterName) {
        config.setClusterName(clusterName);
    }

    /**
     * 添加节点监听器
     *
     * @param nodeChangeListener
     */
    public void addNodeChangeListener(NodeChangeListener nodeChangeListener) {
        registry.addNodeChangeListener(nodeChangeListener);
    }

    /**
     * 添加 master 节点变化监听器
     *
     * @param masterNodeChangeListener
     */
    public void addMasterNodeChangeListener(MasterNodeChangeListener masterNodeChangeListener) {
        application.getMasterElector().addMasterNodeChangeListener(masterNodeChangeListener);
    }
}
