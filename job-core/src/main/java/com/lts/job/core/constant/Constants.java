package com.lts.job.core.constant;


/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 *         一些配置常量
 */
public interface Constants {

    // 可用的处理器个数
    public static final int AVAILABLE_PROCESSOR = Runtime.getRuntime().availableProcessors();

    public static final String USER_HOME = System.getProperty("user.home");

    public static final int JOB_TRACKER_DEFAULT_LISTEN_PORT = 35001;

    // 默认集群名字
    public static final String DEFAULT_CLUSTER_NAME = "defaultCluster";

    // 默认JobTracker节点组
    public static final String DEFAULT_NODE_JOB_TRACKER_GROUP = "jobTrackerGroup";
    // 默认JobClient节点组
    public static final String DEFAULT_NODE_JOB_CLIENT_GROUP = "jobClientGroup";
    // 默认TaskTracker节点组
    public static final String DEFAULT_NODE_TASK_TRACKER_GROUP = "taskTrackerGroup";

    public static final String KEY_AVAILABLE_THREADS = "availableThreads";

    public static final String JOB_RUNNING_CLASS = "JobRunningClass";

    public static final String RUNNER_FACTORY = "RunnerFactory";

    public static final String DEAD_JOB_CHECKER = "DeadJobChecker";

    public static final String REMOTING_SERVER = "RemotingServer";

    public static final String JOB_CLIENT_MANAGER = "JobClientManager";
    public static final String TASK_TRACKER_MANAGER = "TaskTrackerManager";

    public static final String TASK_TRACKER_RUNNER_POOL = "TaskTrackerRunnerPool";

    public static final String CHANNEL_MANAGER = "ChannelManager";

    public static final String JOB_LOGGER = "JobLogger";

}
