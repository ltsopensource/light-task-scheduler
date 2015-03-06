package com.lts.job.spring;

import com.lts.job.core.listener.MasterNodeChangeListener;
import com.lts.job.core.util.Assert;
import com.lts.job.core.util.StringUtils;
import com.lts.job.task.tracker.TaskTracker;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Created by hugui on 3/6/15.
 */
public class TaskTrackerFactoryBean implements FactoryBean<TaskTracker>, InitializingBean, DisposableBean {

    private TaskTracker taskTracker;

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
     * 提交失败任务存储路径 , 默认用户木邻居
     */
    private String jobInfoSavePath;
    /**
     * 工作线程个数
     */
    private int workThreads;
    /**
     * 任务执行类
     */
    private String jobRunnerClass;
    /**
     * master节点变化监听器
     */
    private MasterNodeChangeListener[] masterNodeChangeListeners;

    @Override
    public TaskTracker getObject() throws Exception {
        return taskTracker;
    }

    @Override
    public Class<?> getObjectType() {
        return TaskTracker.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void destroy() throws Exception {
        if (started) {
            taskTracker.stop();
            started = false;
        }
    }

    public void checkProperties() {
        Assert.hasText(nodeGroup, "nodeGroup必须设值!");
        Assert.hasText(zookeeperAddress, "zookeeperAddress必须设值!");
        Assert.isTrue(workThreads > 0, "workThreads必须大于0!");
        Assert.hasText(jobRunnerClass, "jobRunnerClass不能唯恐");
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        checkProperties();

        taskTracker = new TaskTracker();

        if (StringUtils.hasText(clusterName)) {
            taskTracker.setClusterName(clusterName);
        }
        taskTracker.setJobInfoSavePath(jobInfoSavePath);
        taskTracker.setWorkThreads(workThreads);
        taskTracker.setNodeGroup(nodeGroup);
        taskTracker.setZookeeperAddress(zookeeperAddress);
        taskTracker.setJobRunnerClass(Class.forName(jobRunnerClass));

        if (masterNodeChangeListeners != null) {
            for (MasterNodeChangeListener masterNodeChangeListener : masterNodeChangeListeners) {
                taskTracker.addMasterNodeChangeListener(masterNodeChangeListener);
            }
        }
    }

    public void start() {
        if (!started) {
            taskTracker.start();
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

    public void setJobInfoSavePath(String jobInfoSavePath) {
        this.jobInfoSavePath = jobInfoSavePath;
    }

    public void setMasterNodeChangeListeners(MasterNodeChangeListener[] masterNodeChangeListeners) {
        this.masterNodeChangeListeners = masterNodeChangeListeners;
    }

    public void setJobRunnerClass(String jobRunnerClass) {
        this.jobRunnerClass = jobRunnerClass;
    }

    public void setWorkThreads(int workThreads) {
        this.workThreads = workThreads;
    }
}