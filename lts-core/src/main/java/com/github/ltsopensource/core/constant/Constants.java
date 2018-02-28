package com.github.ltsopensource.core.constant;


import java.util.regex.Pattern;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 *         一些配置常量
 */
public interface Constants {

    // 可用的处理器个数
    int AVAILABLE_PROCESSOR = Runtime.getRuntime().availableProcessors();

    String OS_NAME = System.getProperty("os.name");

    String USER_HOME = System.getProperty("user.home");

    String LINE_SEPARATOR = System.getProperty("line.separator");

    int JOB_TRACKER_DEFAULT_LISTEN_PORT = 35001;

    // 默认集群名字
    String DEFAULT_CLUSTER_NAME = "defaultCluster";

    String CHARSET = "UTF-8";

    int DEFAULT_TIMEOUT = 1000;

    int DEFAULT_SESSION_TIMEOUT = 60 * 1000;

    String REGISTER = "register";

    String UNREGISTER = "unregister";

    int DEFAULT_BUFFER_SIZE = 16 * 1024 * 1024;
    /**
     * 重试周期
     */
    int DEFAULT_REGISTRY_RETRY_PERIOD = 5 * 1000;

    Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*[,]+\\s*");

    int DEFAULT_REGISTRY_RECONNECT_PERIOD = 3 * 1000;


    int DEFAULT_JOB_SUBMIT_MAX_QPS = 500;

    int DEFAULT_PROCESSOR_THREAD = 32 + AVAILABLE_PROCESSOR * 5;

    int DEFAULT_JOB_TRACKER_PUSHER_THREAD_NUM = 32 + AVAILABLE_PROCESSOR * 5;

    int LATCH_TIMEOUT_MILLIS = 60 * 1000;      // 60s

    int DEFAULT_JOB_MAX_RETRY_TIMES = 10;

    int DEFAULT_JOB_PULL_FREQUENCY = 1;

    // TaskTracker 离线(网络隔离)时间 10s，超过10s，自动停止当前执行任务
    long DEFAULT_TASK_TRACKER_OFFLINE_LIMIT_MILLIS = 10 * 1000;

    String ADMIN_ID_PREFIX = "LTS_admin_";

    String ADAPTIVE = "adaptive";

    String MACHINE_RES_ENOUGH = "__LTS.INNER.MACHINE.RES.ENOUGH";

    String FIRST_FIRE_TIME = "__LTS_Repeat_Job_First_Fire_Time";

    String ONCE = "__LTS_ONCE";

    String IS_RETRY_JOB = "__LTS_Is_Retry_Job";

    String OLD_PRIORITY = "__LTS_Tmp_Old_Priority";

    // 执行的序号
    String EXE_SEQ_ID = "__LTS_Seq_Id";

    int DEFAULT_JOB_TRACKER_PUSH_BATCH_SIZE = 10;
}
