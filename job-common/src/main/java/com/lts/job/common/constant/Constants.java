package com.lts.job.common.constant;


/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 * 一些配置常量
 */
public interface Constants {

    // 可用的处理器个数
    public static final int AVAILABLE_PROCESSOR = Runtime.getRuntime().availableProcessors();

    public static final String USER_HOME = System.getProperty("user.home");

    public static final int JOB_TRACKER_DEFAULT_LISTEN_PORT = 35001;

    // 默认集群名字
    public static final String DEFAULT_CLUSTER_NAME = "defaultCluster";

    // 默认JobTracker节点组
    public static final String DEFAULT_NODE_JOB_TRACKER_GROUP = "JOB_TRACKER_GROUP";
    // 默认JobClient节点组
    public static final String DEFAULT_NODE_JOB_CLIENT_GROUP = "TASK_TRACKER_GROUP";
    // 默认TaskTracker节点组
    public static final String DEFAULT_NODE_TASK_TRACKER_GROUP = "TASK_TRACKER_GROUP";

    public static interface PropertiesKey{
        public static final String KEY_JOB_WORK_THREADS = "app.job.work.threads";
        public static final String KEY_JOB_NODE_GROUP = "p.job.node.group";
        public static final String KEY_JOB_ZOOKEEPER_ADDRESS = "app.job.zookeeper.address";
        public static final String KEY_JOB_INVOKE_TIMEOUT_MILLIS = "invoke.timeout.millis";
        public static final String KEY_JOB_LISTEN_PORT = "app.job.listen.port";
        public static final String KEY_JOB_INFO_SAVE_PATH = "app.job.info.save.path";
        public static final String KEY_JOB_CLUSTER_NAME = "app.job.cluster.name";
    }
}
