package com.lts.spring;

import com.lts.core.commons.utils.Assert;
import com.lts.core.listener.MasterChangeListener;
import com.lts.jobclient.JobClient;
import com.lts.jobclient.RetryJobClient;
import com.lts.jobclient.support.JobCompletedHandler;
import com.lts.tasktracker.TaskTracker;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;
import java.util.Properties;

/**
 * JobClient Spring Bean 工厂类
 * @author Robert HG (254963746@qq.com) on 8/4/15.
 */
@SuppressWarnings("rawtypes")
public class JobClientFactoryBean implements FactoryBean<JobClient>,
        InitializingBean, DisposableBean {

	private JobClient jobClient;
    private boolean started;
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * 节点组名称
     */
    private String nodeGroup;
    /**
     * zookeeper地址
     */
    private String registryAddress;
    /**
     * 提交失败任务存储路径 , 默认用户木邻居
     */
    private String dataPath;
    /**
     * master节点变化监听器
     */
    private MasterChangeListener[] masterChangeListeners;
    /**
     * 额外参数配置
     */
    private Properties configs = new Properties();
    /**
     * NORMAL, RETRY
     */
    private String type;
    /**
     * 任务完成处理接口
     */
    private JobCompletedHandler jobCompletedHandler;

    @Override
    public JobClient getObject() throws Exception {
        return jobClient;
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
        Assert.hasText(nodeGroup, "nodeGroup must have value.");
        Assert.hasText(registryAddress, "registryAddress must have value.");
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        checkProperties();
        if ("NORMAL".equals(type)) {
            jobClient = new JobClient();
        } else {
            jobClient = new RetryJobClient();
        }

        jobClient.setClusterName(clusterName);
        jobClient.setDataPath(dataPath);
        jobClient.setNodeGroup(nodeGroup);
        jobClient.setRegistryAddress(registryAddress);

        if (jobCompletedHandler != null) {
            jobClient.setJobFinishedHandler(jobCompletedHandler);
        }

        // 设置config
        for (Map.Entry<Object, Object> entry : configs.entrySet()) {
            jobClient.addConfig(entry.getKey().toString(), entry.getValue().toString());
        }

        if (masterChangeListeners != null) {
            for (MasterChangeListener masterChangeListener : masterChangeListeners) {
                jobClient.addMasterChangeListener(masterChangeListener);
            }
        }
    }

    /**
     * 可以自己得到JobTracker对象后调用，也可以直接使用spring配置中的init属性指定该方法
     */
    public void start() {
        if (!started) {
            jobClient.start();
            started = true;
        }
    }

    @Override
    public void destroy() throws Exception {
        jobClient.stop();
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public void setMasterChangeListeners(MasterChangeListener[] masterChangeListeners) {
        this.masterChangeListeners = masterChangeListeners;
    }

    public void setConfigs(Properties configs) {
        this.configs = configs;
    }

    public void setJobCompletedHandler(JobCompletedHandler jobCompletedHandler) {
        this.jobCompletedHandler = jobCompletedHandler;
    }

    public void setType(String type) {
        this.type = type;
    }
}
