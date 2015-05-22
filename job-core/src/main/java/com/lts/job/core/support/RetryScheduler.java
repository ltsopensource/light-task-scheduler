package com.lts.job.core.support;

import com.lts.job.core.Application;
import com.lts.job.core.domain.KVPair;
import com.lts.job.core.extension.ExtensionLoader;
import com.lts.job.core.failstore.FailStore;
import com.lts.job.core.failstore.FailStoreException;
import com.lts.job.core.failstore.FailStoreFactory;
import com.lts.job.core.logger.Logger;
import com.lts.job.core.logger.LoggerFactory;
import com.lts.job.core.util.GenericsUtils;
import com.lts.job.core.util.JSONUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 *         重试定时器 (用来发送 给 客户端的反馈信息等)
 */
public abstract class RetryScheduler<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryScheduler.class);

    private Class<?> type = GenericsUtils.getSuperClassGenericType(this.getClass());

    // 定时检查是否有 师表的反馈任务信息(给客户端的)
    private ScheduledExecutorService RETRY_EXECUTOR_SERVICE;
    private ScheduledFuture<?> scheduledFuture;

    private FailStore failStore;

    // 批量发送的消息数
    private int batchSize = 5;

    public RetryScheduler(Application application) {
        FailStoreFactory failStoreFactory = ExtensionLoader.getExtensionLoader(FailStoreFactory.class).getAdaptiveExtension();
        failStore = failStoreFactory.getFailStore(application.getConfig());
    }

    protected RetryScheduler(Application application, int batchSize) {
        this(application);
        this.batchSize = batchSize;
    }

    public void start() {
        if (RETRY_EXECUTOR_SERVICE == null) {
            RETRY_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
            // 这个时间后面再去优化
            scheduledFuture = RETRY_EXECUTOR_SERVICE.scheduleWithFixedDelay(new CheckRunner(), 30, 30, TimeUnit.SECONDS);
        }
    }

    // 一次最多提交maxSentSize个, 保证文件所也能被其他线程拿到
    private int maxSentSize = 20;

    public void stop() {
        try {
            scheduledFuture.cancel(true);
            RETRY_EXECUTOR_SERVICE.shutdown();
            RETRY_EXECUTOR_SERVICE = null;
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
        }
    }

    /**
     * 定时检查 提交失败任务的Runnable
     */
    private class CheckRunner implements Runnable {

        @Override
        public void run() {
            try {
                // 1. 检测 远程连接 是否可用
                if (!isRemotingEnable()) {
                    return;
                }
                try {
                    failStore.open();
                    int sentSize = 0;

                    List<KVPair<String, T>> kvPairs = failStore.fetchTop(batchSize, type);

                    while (kvPairs != null && kvPairs.size() > 0) {
                        List<T> values = new ArrayList<T>(kvPairs.size());
                        List<String> keys = new ArrayList<String>(kvPairs.size());
                        for (KVPair<String, T> kvPair : kvPairs) {
                            keys.add(kvPair.getKey());
                            values.add(kvPair.getValue());
                        }
                        if (retry(values)) {
                            LOGGER.info("本地任务发送成功, {}", JSONUtils.toJSONString(values));
                            failStore.delete(keys);
                        } else {
                            break;
                        }
                        sentSize += kvPairs.size();
                        if (sentSize >= maxSentSize) {
                            // 一次最多提交maxSentSize个, 保证文件所也能被其他线程拿到
                            break;
                        }
                        kvPairs = failStore.fetchTop(batchSize, type);
                    }
                } finally {
                    failStore.close();
                }
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public void inSchedule(String key, T value) {
        try {
            try {
                failStore.open();
                failStore.put(key, value);
            } finally {
                failStore.close();
            }
        } catch (FailStoreException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 远程连接是否可用
     *
     * @return
     */
    protected abstract boolean isRemotingEnable();

    /**
     * 重试
     *
     * @param list
     * @return
     */
    protected abstract boolean retry(List<T> list);

}
