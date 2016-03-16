package com.lts.spring.quartz;

import com.lts.core.cluster.NodeType;

/**
 *
 * @author Robert HG (254963746@qq.com) on 3/16/16.
 */
class QuartzLTSConfig {

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
     * 提交失败任务存储路径 , 默认用户目录
     */
    private String dataPath;
    /**
     * 这个根据用户配置的Cron任务来
     */
    private int workThreads;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public String getJobClientNodeGroup(){
        return NodeType.JOB_CLIENT + "_" + this.nodeGroup;
    }

    public String getTaskTrackerNodeGroup(){
        return NodeType.TASK_TRACKER + "_" + this.nodeGroup;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public int getWorkThreads() {
        return workThreads;
    }

    public void setWorkThreads(int workThreads) {
        this.workThreads = workThreads;
    }
}
