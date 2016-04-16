package com.github.ltsopensource.spring.quartz;

/**
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

    /**
     * 如果为true, 每次启动时, 则以本地为准, 覆盖lts上的
     * 如果为false,每次启动是, 则以lts为准, 如果lts上已经存在, 则不添加
     */
    private boolean replaceOnExist = true;

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

    public String getJobClientNodeGroup() {
        return "JC_" + this.nodeGroup;
    }

    public String getTaskTrackerNodeGroup() {
        return "TT_" + this.nodeGroup;
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

    public boolean isReplaceOnExist() {
        return replaceOnExist;
    }

    public void setReplaceOnExist(boolean replaceOnExist) {
        this.replaceOnExist = replaceOnExist;
    }
}
