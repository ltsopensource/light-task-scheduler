package com.github.ltsopensource.startup.tasktracker;

import com.github.ltsopensource.core.constant.Level;

import java.util.Map;

/**
 * TaskTracker 配置文件
 *
 * @author Robert HG (254963746@qq.com) on 9/1/15.
 */
@SuppressWarnings("rawtypes")
public class TaskTrackerCfg {

    private String registryAddress;

    private String clusterName;

    private Level bizLoggerLevel;

    private String nodeGroup;

    private int workThreads;

	private Class jobRunnerClass;

    private String dataPath;

    private boolean useSpring = false;

    private String[] springXmlPaths;

    private Map<String, String> configs;

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Map<String, String> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, String> configs) {
        this.configs = configs;
    }

    public Level getBizLoggerLevel() {
        return bizLoggerLevel;
    }

    public void setBizLoggerLevel(Level bizLoggerLevel) {
        this.bizLoggerLevel = bizLoggerLevel;
    }

    public String getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public int getWorkThreads() {
        return workThreads;
    }

    public void setWorkThreads(int workThreads) {
        this.workThreads = workThreads;
    }

    public boolean isUseSpring() {
        return useSpring;
    }

    public void setUseSpring(boolean useSpring) {
        this.useSpring = useSpring;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public Class getJobRunnerClass() {
        return jobRunnerClass;
    }

    public void setJobRunnerClass(Class jobRunnerClass) {
        this.jobRunnerClass = jobRunnerClass;
    }

    public String[] getSpringXmlPaths() {
        return springXmlPaths;
    }

    public void setSpringXmlPaths(String[] springXmlPaths) {
        this.springXmlPaths = springXmlPaths;
    }
}
