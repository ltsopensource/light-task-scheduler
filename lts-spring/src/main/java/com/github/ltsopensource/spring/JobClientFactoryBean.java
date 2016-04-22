package com.github.ltsopensource.spring;

import com.github.ltsopensource.autoconfigure.PropertiesConfigurationFactory;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.listener.MasterChangeListener;
import com.github.ltsopensource.jobclient.JobClient;
import com.github.ltsopensource.jobclient.JobClientBuilder;
import com.github.ltsopensource.core.properties.JobClientProperties;
import com.github.ltsopensource.jobclient.support.JobCompletedHandler;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Properties;

/**
 * JobClient Spring Bean 工厂类
 *
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

    private String identity;

    private String bindIp;
    /**
     * master节点变化监听器
     */
    private MasterChangeListener[] masterChangeListeners;
    /**
     * 额外参数配置
     */
    private Properties configs = new Properties();

    private boolean useRetryClient = true;

    /**
     * 任务完成处理接口
     */
    private JobCompletedHandler jobCompletedHandler;

    private String[] locations;

    @Override
    public JobClient getObject() throws Exception {
        return jobClient;
    }

    @Override
    public Class<?> getObjectType() {
        return JobClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        JobClientProperties properties = null;
        if (locations == null || locations.length == 0) {
            properties = new JobClientProperties();
            properties.setUseRetryClient(useRetryClient);
            properties.setClusterName(clusterName);
            properties.setDataPath(dataPath);
            properties.setNodeGroup(nodeGroup);
            properties.setRegistryAddress(registryAddress);
            properties.setBindIp(bindIp);
            properties.setIdentity(identity);
            properties.setConfigs(CollectionUtils.toMap(configs));

        } else {
            properties = PropertiesConfigurationFactory.createPropertiesConfiguration(JobClientProperties.class, locations);
        }

        jobClient = JobClientBuilder.buildByProperties(properties);

        if (jobCompletedHandler != null) {
            jobClient.setJobCompletedHandler(jobCompletedHandler);
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

    public void setMasterChangeListeners(MasterChangeListener... masterChangeListeners) {
        this.masterChangeListeners = masterChangeListeners;
    }

    public void setConfigs(Properties configs) {
        this.configs = configs;
    }

    public void setJobCompletedHandler(JobCompletedHandler jobCompletedHandler) {
        this.jobCompletedHandler = jobCompletedHandler;
    }

    public void setUseRetryClient(boolean useRetryClient) {
        this.useRetryClient = useRetryClient;
    }

    public void setLocations(String... locations) {
        this.locations = locations;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public void setBindIp(String bindIp) {
        this.bindIp = bindIp;
    }
}