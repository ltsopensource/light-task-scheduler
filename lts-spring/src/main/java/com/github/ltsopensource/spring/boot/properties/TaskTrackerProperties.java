package com.github.ltsopensource.spring.boot.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@ConfigurationProperties(prefix = "lts.tasktracker")
public class TaskTrackerProperties extends AbstractProperties {

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

    private DispatchRunner dispatchRunner;

    public DispatchRunner getDispatchRunner() {
        return dispatchRunner;
    }

    public void setDispatchRunner(DispatchRunner dispatchRunner) {
        this.dispatchRunner = dispatchRunner;
    }

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
