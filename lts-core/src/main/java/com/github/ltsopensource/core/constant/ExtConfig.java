package com.github.ltsopensource.core.constant;

/**
 * @author Robert HG (254963746@qq.com) on 4/23/16.
 */
public interface ExtConfig {

    // 是否延迟批量刷盘日志, 如果启用，采用队列的方式批量将日志刷盘(在应用关闭的时候，可能会造成日志丢失) , 默认关闭
    String LAZY_JOB_LOGGER = "lazy.job.logger";
    // 延迟批量刷盘日志 内存中的最大日志量阀值
    String LAZY_JOB_LOGGER_MEM_SIZE = "lazy.job.logger.mem.size";
    // 延迟批量刷盘日志 检查频率
    String LAZY_JOB_LOGGER_CHECK_PERIOD = "lazy.job.logger.check.period";
    String LAZY_JOB_LOGGER_BATCH_FLUSH_SIZE = "lazy.job.logger.batch.flush.size";
    String LAZY_JOB_LOGGER_OVERFLOW_SIZE = "lazy.job.logger.overflow.size";
    /**
     * JobClient,JobTracker,TaskTracker端: 远程通讯请求处理线程数量, 默认 32 + AVAILABLE_PROCESSOR * 5
     */
    String PROCESSOR_THREAD = "lts.job.processor.thread";
    /**
     * JobClient,JobTracker,TaskTracker端: Java 编译器, 可选值 jdk, javassist, 默认 javassist
     */
    String COMPILER = "java.compiler";
    /**
     * JobClient,JobTracker,TaskTracker端: 远程通讯序列化方式, 可选值 fastjson, hessian2, java, 默认fastjson
     */
    String REMOTING_SERIALIZABLE_DFT = "lts.remoting.serializable.default";
    /**
     * JobClient,TaskTracker端: FailStore,可选值 leveldb, berkeleydb,rocksdb ,mapdb  默认 leveldb
     */
    String FAIL_STORE = "job.fail.store";
    /**
     * JobClient,JobTracker,TaskTracker端: 事件中心,不要设置
     */
    String EVENT_CENTER = "event.center";
    /**
     * JDBC提供者, 默认mysql(目前只有mysql实现, 所以不用设置)
     */
    String JDBC_DATASOURCE_PROVIDER = "jdbc.datasource.provider";
    /**
     * LTS远程通讯框架: netty mina  默认 netty
     */
    String REMOTING = "lts.remoting";
    /**
     * 所有端: 链接zk的客户端, 可选值 zkclient, curator, lts 默认 zkclient
     */
    String ZK_CLIENT_KEY = "zk.client";
    /**
     * JobTracker端: 任务biz logger 可选值 console, mysql, mongo 默认 mysql
     */
    String JOB_LOGGER = "job.logger";
    /**
     * 各端: LTS的logger,可选值 slf4j, jcl, log4j, jdk 默认加载顺序 slf4j > jcl > log4j > jdk
     */
    String LTS_LOGGER = "lts.logger";
    /**
     * 任务队列, 可选值 mysql, mongo 默认 mysql
     */
    String JOB_QUEUE = "job.queue";
    /**
     * LTS 内部使用的json, 默认fastjson
     */
    String LTS_JSON = "lts.json";
    /**
     * Admin和Monitor端: 使用的数据存储,目前只有mysql实现
     */
    String ACCESS_DB = "lts.admin.access.db";
    /**
     * 注册中心自动重连时间
     */
    String REGISTRY_RECONNECT_PERIOD_KEY = "reconnect.period";
    /**
     * 注册中心失败事件重试事件
     */
    String REGISTRY_RETRY_PERIOD_KEY = "retry.period";
    String REDIS_SESSION_TIMEOUT = "redis.session.timeout";
    /**
     * JDBC链接相关配置
     */
    String JDBC_URL = "jdbc.url";
    String JDBC_USERNAME = "jdbc.username";
    String JDBC_PASSWORD = "jdbc.password";

    String NEED_CREATE_DB_TABLE = "jdbc.create.db.table";
    /**
     * Durid相关数据的配置
     */
    String DRUID_initialSize = "druid.initialSize";
    String DRUID_maxActive = "druid.maxActive";
    String DRUID_maxIdle = "druid.maxIdle";
    String DRUID_minIdle = "druid.minIdle";
    String DRUID_maxWait = "druid.maxWait";
    String DRUID_poolPreparedStatements = "druid.poolPreparedStatements";
    String DRUID_maxOpenPreparedStatements = "druid.maxOpenPreparedStatements";
    String DRUID_validationQuery = "druid.validationQuery";
    String DRUID_testOnBorrow = "druid.testOnBorrow";
    String DRUID_testOnReturn = "druid.testOnReturn";
    String DRUID_testWhileIdle = "druid.testWhileIdle";
    String DRUID_timeBetweenEvictionRunsMillis = "druid.timeBetweenEvictionRunsMillis";
    String DRUID_numTestsPerEvictionRun = "druid.numTestsPerEvictionRun";
    String DRUID_minEvictableIdleTimeMillis = "druid.minEvictableIdleTimeMillis";
    String DRUID_exceptionSorter = "druid.exceptionSorter";
    String DRUID_filters = "druid.filters";

    /**
     * mongo相关配置
     */
    String MONGO_ADDRESSES = "mongo.addresses";
    String MONGO_DATABASE = "mongo.database";
    String MONGO_USERNAME = "mongo.username";
    String MONGO_PASSWORD = "mongo.password";

    /**
     * JobClient,JobTracker,TaskTracker, Monitor端: Http Cmd 端口
     */
    String HTTP_CMD_PORT = "lts.http.cmd.port";
    /**
     * TaskTracker端: 是否开启网络隔离, 自杀程序, TaskTracker超过一定时间断线JobTracker，自动停止当前的所有任务
     */
    String TASK_TRACKER_STOP_WORKING_ENABLE = "tasktracker.stop.working.enable";
    /**
     * JobTracker端: 不依赖上周期任务的生成调度时间, 默认10分钟 (不建议自己设置)
     */
    String JOB_TRACKER_NON_RELYON_PREV_CYCLE_JOB_SCHEDULER_INTERVAL_MINUTE = "jobtracker.nonRelyOnPrevCycleJob.schedule.interval.minute";
    /**
     * JobClient,JobTracker,TaskTracker端: 向monitor汇报数据间隔
     */
    String LTS_MONITOR_REPORT_INTERVAL = "lts.monitor.report.interval";
    /**
     * JobTracker端: 最大 Job preload 的 size , 默认 300
     */
    String JOB_TRACKER_PRELOADER_SIZE = "job.preloader.size";
    /**
     * JobTracker端: Job preload 的 阀值  默认 0.2 (20%)
     */
    String JOB_TRACKER_PRELOADER_FACTOR = "job.preloader.factor";
    /**
     * JobTracker端: Job preload 信号检测频率
     */
    String JOB_TRACKER_PRELOADER_SIGNAL_CHECK_INTERVAL = "job.preloader.signal.check.interval";
    /**
     * Netty Frame 的最大长度(自己一般不用设置)
     */
    String NETTY_FRAME_LENGTH_MAX = "netty.frame.length.max";
    /**
     * JobClient端: 提交并发请求size
     */
    String JOB_SUBMIT_MAX_QPS = "job.submit.maxQPS";
    /**
     * JobClient端: 提交任务获取 lock的 timeout (毫秒)
     */
    String JOB_SUBMIT_LOCK_ACQUIRE_TIMEOUT = "job.submit.lock.acquire.timeout";
    /**
     * JobTracker端: 任务重试时间间隔, 默认 30s
     */
    String JOB_TRACKER_JOB_RETRY_INTERVAL_MILLIS = "jobtracker.job.retry.interval.millis";
    /**
     * JobTracker端:设置任务最多重试次数, 默认10次
     */
    String JOB_MAX_RETRY_TIMES = "job.max.retry.times";
    /**
     * JobTracker端: 是否开启远程请求最大QPS限流
     */
    String JOB_TRACKER_REMOTING_REQ_LIMIT_ENABLE = "remoting.req.limit.enable";
    /**
     * JobTracker端: 远程请求最大QPS限流, 默认 5000
     */
    String JOB_TRACKER_REMOTING_REQ_LIMIT_MAX_QPS = "remoting.req.limit.maxQPS";
    /**
     * JobTracker端: 远程请求的lock获取 timeout, 默认 50毫秒 (不建议自己设置)
     */
    String JOB_TRACKER_REMOTING_REQ_LIMIT_ACQUIRE_TIMEOUT = "remoting.req.limit.acquire.timeout";
    /**
     * JobTracker端: 正在执行任务队列中死任务的检查频率
     */
    String JOB_TRACKER_EXECUTING_JOB_FIX_CHECK_INTERVAL_SECONDS = "jobtracker.executing.job.fix.check.interval.seconds";
    /**
     * JobTracker端: 正在执行任务修复死任务检查的时间限制(不建议自己设置)
     */
    String JOB_TRACKER_EXECUTING_JOB_FIX_DEADLINE_SECONDS = "jobtracker.executing.job.fix.deadline.seconds";
    /**
     * TaskTracker端: Pull 任务频率(秒) , 默认 1s(不建议自己设置)
     */
    String JOB_PULL_FREQUENCY = "job.pull.frequency";
    /**
     * TaskTracker端: 是否启用TaskTracker端的负载均衡, 默认关闭
     */
    String LB_MACHINE_RES_CHECK_ENABLE = "lb.machine.res.check.enable";
    /**
     * TaskTracker端: 负载均衡, 最大内存使用率,超过该使用率,停止pull任务, 默认 0.9(90%)
     */
    String LB_MEMORY_USED_RATE_MAX = "lb.memoryUsedRate.max";
    /**
     * TaskTracker端: 负载均衡, 最大cpu使用率,超过该使用率,停止pull任务, 默认 0.9 (90%)
     */
    String LB_CPU_USED_RATE_MAX = "lb.cpuUsedRate.max";
    /**
     * JobClient,JobTracker,TaskTracker端: 各个节点选择连接Monitor的负载均衡算法
     */
    String MONITOR_SELECT_LOADBALANCE = "monitor.select.loadbalance";
    /**
     * JobClient,TaskTracker端: 选择JobTracker的负载均衡算法
     */
    String JOB_TRACKER_SELECT_LOADBALANCE = "jobtracker.select.loadbalance";
    /**
     * JobTracker端: 选择jobClient的负载均衡算法
     */
    String JOB_CLIENT_SELECT_LOADBALANCE = "jobclient.select.loadbalance";
    /**
     * JobTracker端: 修改ExecutingJobQueue死任务的时候, 等待等待完成任务的时间
     */
    String JOB_TRACKER_FIX_EXECUTING_JOB_WAITING_MILLS = "jobtracker.fix.executing.job.waiting.mills";

    String LOADBALANCE = "loadbalance";

    String MAIL_SMTP_HOST = "mail.smtp.host";
    String MAIL_SMTP_PORT = "mail.smtp.port";
    String MAIL_USERNAME = "mail.username";
    String MAIL_PASSWORD = "mail.password";
    String MAIL_ADMIN_ADDR = "mail.adminAddress";
    String MAIL_SSL_ENABLED = "mail.sslEnabled";
    String JOB_RETRY_TIME_GENERATOR = "jobtracker.retry.time.generator";

    String M_STAT_REPORTER_CLOSED  = "mStatReporterClosed";

    String JOB_TRACKER_PUSHER_THREAD_NUM = "lts.job.tracker.pusher.thread.num";

    String JOB_TRACKER_PUSH_BATCH_SIZE = "lts.job.tracker.push.batch.size";

    String TASK_TRACKER_BIZ_LOGGER_FAIL_STORE_CLOSE = "lts.task.tracker.biz.logger.failstore.close";

    String TASK_TRACKER_JOB_RESULT_FAIL_STORE_CLOSE = "lts.task.tracker.job.result.failstore.close";

}
