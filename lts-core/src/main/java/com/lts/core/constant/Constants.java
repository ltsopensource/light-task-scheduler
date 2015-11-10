package com.lts.core.constant;


import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 *         一些配置常量
 */
public interface Constants {

    // 可用的处理器个数
    int AVAILABLE_PROCESSOR = Runtime.getRuntime().availableProcessors();

    String USER_HOME = System.getProperty("user.home");

    int JOB_TRACKER_DEFAULT_LISTEN_PORT = 35001;

    // 默认集群名字
    String DEFAULT_CLUSTER_NAME = "defaultCluster";

    String CHARSET = "UTF-8";

    int DEFAULT_TIMEOUT = 1000;

    String TIMEOUT_KEY = "timeout";

    String SESSION_TIMEOUT_KEY = "session";

    int DEFAULT_SESSION_TIMEOUT = 60 * 1000;

    String REGISTER = "register";

    String UNREGISTER = "unregister";

    String SUBSCRIBE = "subscribe";

    String UNSUBSCRIBE = "unsubscribe";

    int DEFAULT_BUFFER_SIZE = 16 * 1024;
    /**
     * 注册中心失败事件重试事件
     */
    String REGISTRY_RETRY_PERIOD_KEY = "retry.period";

    /**
     * 重试周期
     */
    int DEFAULT_REGISTRY_RETRY_PERIOD = 5 * 1000;

    Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*[,]+\\s*");

    /**
     * 注册中心自动重连时间
     */
    String REGISTRY_RECONNECT_PERIOD_KEY = "reconnect.period";

    int DEFAULT_REGISTRY_RECONNECT_PERIOD = 3 * 1000;

    String ZK_CLIENT_KEY = "zk.client";

    String JOB_LOGGER_KEY = "job.logger";

    String JOB_QUEUE_KEY = "job.queue";

    // 客户端提交并发请求size
    String JOB_SUBMIT_CONCURRENCY_SIZE = "job.submit.concurrency.size";
    int DEFAULT_JOB_SUBMIT_CONCURRENCY_SIZE = 100;

    String PROCESSOR_THREAD = "job.processor.thread";
    int DEFAULT_PROCESSOR_THREAD = 32 + AVAILABLE_PROCESSOR * 5;

    int LATCH_TIMEOUT_MILLIS = 10 * 60 * 1000;      // 10分钟

    // 任务最多重试次数
    String JOB_MAX_RETRY_TIMES = "job.max.retry.times";
    int DEFAULT_JOB_MAX_RETRY_TIMES = 10;

    Charset UTF_8 = Charset.forName("UTF-8");

    String MONITOR_DATA_ADD_URL = "/api/monitor/monitor-data-add.do";

    String MONITOR_JVM_INFO_DATA_ADD_URL = "/api/monitor/jvm-info-data-add.do";

    String MONITOR_COMMAND_INFO_ADD_URL = "/api/monitor/command-info-add.do";

    String JOB_PULL_FREQUENCY = "job.pull.frequency";
    int DEFAULT_JOB_PULL_FREQUENCY = 1;

    // TaskTracker 离线(网络隔离)时间 2 分钟，超过两分钟，自动停止当前执行任务
    long TASK_TRACKER_OFFLINE_LIMIT_MILLIS = 2 * 60 * 1000;
    // TaskTracker超过一定时间断线JobTracker，自动停止当前的所有任务
    String TASK_TRACKER_STOP_WORKING_SWITCH = "stop.working";

    String ADMIN_ID_PREFIX = "LTS_admin_";

    // 是否延迟批量刷盘日志, 如果启用，采用队列的方式批量将日志刷盘(在应用关闭的时候，可能会造成日志丢失)
    String LAZY_JOB_LOGGER = "lazy.job.logger";
    // 延迟批量刷盘日志 内存中的最大日志量阀值
    String LAZY_JOB_LOGGER_MEM_SIZE = "lazy.job.logger.mem.size";
    // 延迟批量刷盘日志 检查频率
    String LAZY_JOB_LOGGER_CHECK_PERIOD = "lazy.job.logger.check.period";

    String DEFAULT_REMOTING_SERIALIZABLE = "lts.remoting.serializable.default";

}
