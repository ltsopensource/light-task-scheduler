package com.lts.job.common.cluster;

import com.lts.job.common.AppConfigure;
import com.lts.job.common.constant.Constants;
import com.lts.job.common.domain.JobNodeConfig;
import com.lts.job.common.listener.MasterNodeChangeListener;
import com.lts.job.common.listener.NodeChangeListener;
import com.lts.job.common.registry.NodeRegistry;
import com.lts.job.common.support.Application;
import com.lts.job.common.listener.MasterNodeElectionListener;
import com.lts.job.common.util.GenericsUtils;
import com.lts.job.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert HG (254963746@qq.com) on 8/15/14.
 * 抽象节点
 */
public abstract class AbstractJobNode<T extends Node> implements JobNode {

    protected static final Logger LOGGER = LoggerFactory.getLogger(JobNode.class);

    protected NodeRegistry registry;
    protected T node;
    protected JobNodeConfig config;

    public AbstractJobNode() {
        config = new JobNodeConfig();
        config.setIdentity(StringUtils.generateUUID());
        config.setWorkThreads(AppConfigure.getInteger(Constants.PropertiesKey.KEY_JOB_WORK_THREADS, Constants.AVAILABLE_PROCESSOR));
        config.setNodeGroup(AppConfigure.getString(Constants.PropertiesKey.KEY_JOB_NODE_GROUP));
        config.setZookeeperAddress(AppConfigure.getString(Constants.PropertiesKey.KEY_JOB_ZOOKEEPER_ADDRESS));
        config.setInvokeTimeoutMillis(AppConfigure.getInteger(Constants.PropertiesKey.KEY_JOB_INVOKE_TIMEOUT_MILLIS, 1000 * 6));
        config.setListenPort(AppConfigure.getInteger(Constants.PropertiesKey.KEY_JOB_LISTEN_PORT, 0));
        config.setJobInfoSavePath(AppConfigure.getString(Constants.PropertiesKey.KEY_JOB_INFO_SAVE_PATH, Constants.USER_HOME + "/.job"));
        config.setClusterName(AppConfigure.getString(Constants.PropertiesKey.KEY_JOB_CLUSTER_NAME, Constants.DEFAULT_CLUSTER_NAME));
        // 可用的线程数
        Application.setAttribute(Application.KEY_AVAILABLE_THREADS, config.getWorkThreads());

        Application.Config = config;

        this.registry = new NodeRegistry();
        // 用于master选举的监听器
        addNodeChangeListener(new MasterNodeElectionListener());
    }

    final public void start() {
        try {

            Class<T> nodeClass = GenericsUtils.getSuperClassGenericType(this.getClass());
            node = NodeFactory.create(nodeClass, config);
            config.setNodeType(node.getNodeType());

            LOGGER.info("当前节点配置:{}", config);

            nodeStart();

            registry.register(node);
            LOGGER.info("启动成功!");

        } catch (Throwable e) {
            LOGGER.error("启动失败!", e);
        }
    }

    final public void stop() {
        try {
            registry.unregister(node);
            nodeStop();
            LOGGER.info("停止成功!");
        } catch (Throwable e) {
            LOGGER.error("停止失败!", e);
        }
    }

    protected abstract void nodeStart();

    protected abstract void nodeStop();

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
        MasterElector.addMasterNodeChangeListener(masterNodeChangeListener);
    }
}
