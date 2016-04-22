package com.github.ltsopensource.core.properties;


import com.github.ltsopensource.autoconfigure.annotation.ConfigurationProperties;
import com.github.ltsopensource.core.cluster.AbstractConfigProperties;
import com.github.ltsopensource.core.commons.utils.Assert;
import com.github.ltsopensource.core.constant.Level;
import com.github.ltsopensource.core.exception.ConfigPropertiesIllegalException;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@ConfigurationProperties(prefix = "lts.tasktracker")
public class TaskTrackerProperties extends AbstractConfigProperties {

    /**
     * 节点Group
     */
    private String nodeGroup;
    /**
     * FailStore数据存储路径
     */
    private String dataPath;
    /**
     * 工作线程,默认64
     */
    private int workThreads;

    private Level bizLoggerLevel;

    private DispatchRunner dispatchRunner;

    private Class<?> jobRunnerClass;

    public String getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
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

    public Class<?> getJobRunnerClass() {
        return jobRunnerClass;
    }

    public void setJobRunnerClass(Class<?> jobRunnerClass) {
        this.jobRunnerClass = jobRunnerClass;
    }

    public Level getBizLoggerLevel() {
        return bizLoggerLevel;
    }

    public void setBizLoggerLevel(Level bizLoggerLevel) {
        this.bizLoggerLevel = bizLoggerLevel;
    }

    public DispatchRunner getDispatchRunner() {
        return dispatchRunner;
    }

    public void setDispatchRunner(DispatchRunner dispatchRunner) {
        this.dispatchRunner = dispatchRunner;
    }

    @Override
    public void checkProperties() throws ConfigPropertiesIllegalException {
        Assert.hasText(getClusterName(), "clusterName must have value.");
        Assert.hasText(getNodeGroup(), "nodeGroup must have value.");
        Assert.hasText(getRegistryAddress(), "registryAddress must have value.");
        Assert.isTrue(getWorkThreads() >= 0, "workThreads must >= 0.");
    }

    public static class DispatchRunner {
        /**
         * 是否使用shardRunner
         */
        private boolean enable = false;
        /**
         * shard的字段,默认taskId
         */
        private String shardValue;

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public String getShardValue() {
            return shardValue;
        }

        public void setShardValue(String shardValue) {
            this.shardValue = shardValue;
        }
    }
}
