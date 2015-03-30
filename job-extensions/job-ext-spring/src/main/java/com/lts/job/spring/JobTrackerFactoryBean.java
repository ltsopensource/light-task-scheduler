package com.lts.job.spring;

import com.lts.job.core.listener.MasterNodeChangeListener;
import com.lts.job.core.util.Assert;
import com.lts.job.core.util.StringUtils;
import com.lts.job.tracker.JobTracker;
import com.lts.job.tracker.logger.JobLogger;
import com.lts.job.tracker.queue.JobFeedbackQueue;
import com.lts.job.tracker.queue.JobQueue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * JobClient 的 FactoryBean
 * @author Robert HG (254963746@qq.com) on 3/6/15.
 */
public class JobTrackerFactoryBean implements FactoryBean<JobTracker>, InitializingBean, DisposableBean {

    private JobTracker jobTracker;

    private volatile boolean started;
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * 监听端口
     */
    private Integer listenPort;
    /**
     * zookeeper地址
     */
    private String zookeeperAddress;
    /**
     * master节点变化监听器
     */
    private MasterNodeChangeListener[] masterNodeChangeListeners;

    private JobLogger jobLogger;

    private JobQueue jobQueue;

    private JobFeedbackQueue jobFeedbackQueue;

    @Override
    public JobTracker getObject() throws Exception {
        return jobTracker;
    }

    @Override
    public Class<?> getObjectType() {
        return JobTracker.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void destroy() throws Exception {
        if (started) {
            jobTracker.stop();
            started = false;
        }
    }

    public void checkProperties() {
        Assert.hasText(zookeeperAddress, "zookeeperAddress必须设值!");
        if (listenPort != null && listenPort <= 0) {
            listenPort = null;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        checkProperties();

        jobTracker = new JobTracker();

        if (StringUtils.hasText(clusterName)) {
            jobTracker.setClusterName(clusterName);
        }
        jobTracker.setZookeeperAddress(zookeeperAddress);
        if (listenPort != null) {
            jobTracker.setListenPort(listenPort);
        }
        if (masterNodeChangeListeners != null) {
            for (MasterNodeChangeListener masterNodeChangeListener : masterNodeChangeListeners) {
                jobTracker.addMasterNodeChangeListener(masterNodeChangeListener);
            }
        }
        jobTracker.setJobLogger(jobLogger);
        jobTracker.setJobQueue(jobQueue);
        jobTracker.setJobFeedbackQueue(jobFeedbackQueue);
    }

    public void start() {
        if (!started) {
            jobTracker.start();
            started = true;
        }
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setListenPort(Integer listenPort) {
        this.listenPort = listenPort;
    }

    public void setZookeeperAddress(String zookeeperAddress) {
        this.zookeeperAddress = zookeeperAddress;
    }

    public void setMasterNodeChangeListeners(MasterNodeChangeListener[] masterNodeChangeListeners) {
        this.masterNodeChangeListeners = masterNodeChangeListeners;
    }

    public void setJobLogger(JobLogger jobLogger) {
        this.jobLogger = jobLogger;
    }

    public void setJobQueue(JobQueue jobQueue) {
        this.jobQueue = jobQueue;
    }

    public void setJobFeedbackQueue(JobFeedbackQueue jobFeedbackQueue) {
        this.jobFeedbackQueue = jobFeedbackQueue;
    }
}
