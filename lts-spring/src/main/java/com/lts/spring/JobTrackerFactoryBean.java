package com.lts.spring;

import com.lts.core.commons.utils.Assert;
import com.lts.core.listener.MasterChangeListener;
import com.lts.jobtracker.JobTracker;
import com.lts.jobtracker.support.OldDataHandler;
import com.lts.jobtracker.support.policy.OldDataDeletePolicy;
import com.lts.tasktracker.TaskTracker;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;
import java.util.Properties;

/**
 * JobTracker Spring Bean 工厂类
 * @author Robert HG (254963746@qq.com) on 8/4/15.
 */
public class JobTrackerFactoryBean implements FactoryBean<JobTracker>,
        InitializingBean, DisposableBean {

    private JobTracker jobTracker;
    private boolean started;
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * zookeeper地址
     */
    private String registryAddress;
    /**
     * master节点变化监听器
     */
    private MasterChangeListener[] masterChangeListeners;
    /**
     * 额外参数配置
     */
    private Properties configs = new Properties();
    /**
     * 监听端口
     */
    private Integer listenPort;
    /**
     * 老数据处理接口
     */
    private OldDataHandler oldDataHandler;

    @Override
    public JobTracker getObject() throws Exception {
        return jobTracker;
    }

    @Override
    public Class<?> getObjectType() {
        return TaskTracker.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void checkProperties() {
        Assert.hasText(clusterName, "clusterName must have value.");
        Assert.hasText(registryAddress, "registryAddress must have value.");
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        checkProperties();

        jobTracker = new JobTracker();

        jobTracker.setClusterName(clusterName);
        jobTracker.setRegistryAddress(registryAddress);

        if (listenPort != null) {
            jobTracker.setListenPort(listenPort);
        }
        if (oldDataHandler == null) {
            jobTracker.setOldDataHandler(new OldDataDeletePolicy());
        } else {
            jobTracker.setOldDataHandler(oldDataHandler);
        }

        // 设置config
        for (Map.Entry<Object, Object> entry : configs.entrySet()) {
            jobTracker.addConfig(entry.getKey().toString(), entry.getValue().toString());
        }

        if (masterChangeListeners != null) {
            for (MasterChangeListener masterChangeListener : masterChangeListeners) {
                jobTracker.addMasterChangeListener(masterChangeListener);
            }
        }
    }

    /**
     * 可以自己得到JobTracker对象后调用，也可以直接使用spring配置中的init属性指定该方法
     */
    public void start() {
        if (!started) {
            jobTracker.start();
            started = true;
        }
    }

    @Override
    public void destroy() throws Exception {
        jobTracker.stop();
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void setMasterChangeListeners(MasterChangeListener[] masterChangeListeners) {
        this.masterChangeListeners = masterChangeListeners;
    }

    public void setConfigs(Properties configs) {
        this.configs = configs;
    }

    public void setOldDataHandler(OldDataHandler oldDataHandler) {
        this.oldDataHandler = oldDataHandler;
    }

    public void setListenPort(Integer listenPort) {
        this.listenPort = listenPort;
    }
}
