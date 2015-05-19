package com.lts.job.core.constant;


import java.util.regex.Pattern;

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

    public static final String CHARSET = "utf-8";

    public static final int DEFAULT_TIMEOUT = 1000;

    public static final String TIMEOUT_KEY = "timeout";

    public static final String SESSION_TIMEOUT_KEY = "session";

    public static final int DEFAULT_SESSION_TIMEOUT = 60 * 1000;

    public static final String REGISTER = "register";

    public static final String UNREGISTER = "unregister";

    /**
     * 注册中心失败事件重试事件
     */
    public static final String REGISTRY_RETRY_PERIOD_KEY = "retry.period";

    /**
     * 重试周期
     */
    public static final int DEFAULT_REGISTRY_RETRY_PERIOD = 5 * 1000;

    public static final Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*[,]+\\s*");

    /**
     * 注册中心自动重连时间
     */
    public static final String REGISTRY_RECONNECT_PERIOD_KEY = "reconnect.period";

    public static final int DEFAULT_REGISTRY_RECONNECT_PERIOD = 3 * 1000;

    public static final String ZK_CLIENT_KEY = "zk.client";

    public static final String JOB_LOGGER_KEY = "job.logger";

    public static final String JOB_QUEUE_KEY = "job.queue";
}
