package com.lts.job.spring;

import com.lts.job.client.JobClient;
import com.lts.job.client.RetryJobClient;
import com.lts.job.client.support.JobFinishedHandler;
import com.lts.job.core.listener.MasterNodeChangeListener;
import com.lts.job.core.util.Assert;
import com.lts.job.core.util.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * JobClient 的 FactoryBean
 * @author Robert HG (254963746@qq.com) on 3/6/15.
 */
public class JobClientFactoryBean implements FactoryBean<JobClient>, InitializingBean, DisposableBean {

    private JobClient jobClient;

    private JobClientType clientType;

    private volatile boolean started;
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
    private String zookeeperAddress;
    /**
     * 任务完成处理器
     */
    private JobFinishedHandler jobFinishedHandler;
    /**
     * 提交失败任务存储路径 , 默认用户木邻居
     */
    private String jobInfoSavePath;
    /**
     * master节点变化监听器
     */
    private MasterNodeChangeListener[] masterNodeChangeListeners;

    public void setClientType(String type) {
        if (type == null) {
            clientType = JobClientType.NORMAL;
        }
        try {
            clientType = JobClientType.parse(type);
        } catch (Exception e) {
            throw new IllegalArgumentException("clientType 为空或者 normal, retry");
        }
    }

    @Override
    public JobClient getObject() throws Exception {
        return jobClient;
    }

    @Override
    public Class<?> getObjectType() {
        if (JobClientType.NORMAL.equals(clientType)) {
            return JobClient.class;
        }
        return RetryJobClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void destroy() throws Exception {
        if (started) {
            jobClient.stop();
            started = false;
        }
    }

    public void checkProperties() {
        if (clientType == null) {
            clientType = JobClientType.NORMAL;
        }
        Assert.hasText(nodeGroup, "nodeGroup必须设值!");
        Assert.hasText(zookeeperAddress, "zookeeperAddress必须设值!");
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        checkProperties();

        if (JobClientType.NORMAL.equals(clientType)) {
            jobClient = new JobClient();
        }
        jobClient = new RetryJobClient();

        if (StringUtils.hasText(clusterName)) {
            jobClient.setClusterName(clusterName);
        }
        jobClient.setJobInfoSavePath(jobInfoSavePath);
        jobClient.setNodeGroup(nodeGroup);
        if (jobFinishedHandler != null) {
            jobClient.setJobFinishedHandler(jobFinishedHandler);
        }
        jobClient.setZookeeperAddress(zookeeperAddress);

        if (masterNodeChangeListeners != null) {
            for (MasterNodeChangeListener masterNodeChangeListener : masterNodeChangeListeners) {
                jobClient.addMasterNodeChangeListener(masterNodeChangeListener);
            }
        }
    }

    public void start() {
        if (!started) {
            jobClient.start();
            started = true;
        }
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public void setZookeeperAddress(String zookeeperAddress) {
        this.zookeeperAddress = zookeeperAddress;
    }

    public void setJobFinishedHandler(JobFinishedHandler jobFinishedHandler) {
        this.jobFinishedHandler = jobFinishedHandler;
    }

    public void setJobInfoSavePath(String jobInfoSavePath) {
        this.jobInfoSavePath = jobInfoSavePath;
    }

    public void setMasterNodeChangeListeners(MasterNodeChangeListener[] masterNodeChangeListeners) {
        this.masterNodeChangeListeners = masterNodeChangeListeners;
    }
}

enum JobClientType {

    NORMAL("normal"),       // 正常的
    RETRY("retry");         // 重试的

    private String value;

    JobClientType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public static JobClientType parse(String value) {
        for (JobClientType jobClientType : JobClientType.values()) {
            if (jobClientType.value.equals(value)) {
                return jobClientType;
            }
        }
        throw new IllegalArgumentException("value" + value + "错误");
    }
}
