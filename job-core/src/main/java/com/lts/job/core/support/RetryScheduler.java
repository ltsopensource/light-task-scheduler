package com.lts.job.core.support;

import com.lts.job.core.Application;
import com.lts.job.core.domain.KVPair;
import com.lts.job.core.file.FileAccessor;
import com.lts.job.core.file.FileException;
import com.lts.job.core.util.GenericsUtils;
import com.lts.job.core.util.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 *         重试定时器 (用来发送 给 客户端的反馈信息等)
 */
public abstract class RetryScheduler<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryScheduler.class);

    private Class<T> clazz = GenericsUtils.getSuperClassGenericType(this.getClass());

    // 定时检查是否有 师表的反馈任务信息(给客户端的)
    private ScheduledExecutorService RETRY_EXECUTOR_SERVICE;
    private LevelDBStore levelDBStore;
    // 文件锁 (同一时间只能有一个线程在 检查提交失败的任务)
    private com.lts.job.core.file.FileAccessor dbLock;

    // 批量发送的消息数
    private int batchSize = 5;

    public RetryScheduler(Application application) {
        try {
            levelDBStore = new LevelDBStore(application.getConfig().getFilePath());
            dbLock = new FileAccessor(application.getConfig().getFilePath() + "___db.lock");
        } catch (FileException e) {
            throw new RuntimeException(e);
        }
    }

    protected RetryScheduler(Application application, int batchSize) {
        this(application);
        this.batchSize = batchSize;
    }

    public void start() {
        if (levelDBStore != null) {

            dbLock.createIfNotExist();

            RETRY_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
            // 这个时间后面再去优化
            RETRY_EXECUTOR_SERVICE.scheduleWithFixedDelay(new CheckRunner(), 30, 30, TimeUnit.SECONDS);
        }
    }

    // 一次最多提交maxSentSize个, 保证文件所也能被其他线程拿到
    private int maxSentSize = 20;

    public void stop() {
        RETRY_EXECUTOR_SERVICE.shutdown();
        RETRY_EXECUTOR_SERVICE = null;
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
                dbLock.tryLock();
                try {
                    levelDBStore.open();
                    int sentSize = 0;

                    List<KVPair<String, T>> kvPairs = levelDBStore.getList(batchSize, clazz);

                    while (kvPairs != null && kvPairs.size() > 0) {
                        List<T> values = new ArrayList<T>(kvPairs.size());
                        List<String> keys = new ArrayList<String>(kvPairs.size());
                        for (KVPair<String, T> kvPair : kvPairs) {
                            keys.add(kvPair.getKey());
                            values.add(kvPair.getValue());
                        }
                        if (retry(values)) {
                            LOGGER.info("本地任务发送成功, {}", JSONUtils.toJSONString(values));
                            levelDBStore.delete(keys);
                        } else {
                            break;
                        }
                        sentSize += kvPairs.size();
                        if (sentSize >= maxSentSize) {
                            // 一次最多提交maxSentSize个, 保证文件所也能被其他线程拿到
                            break;
                        }
                        kvPairs = levelDBStore.getList(batchSize, clazz);
                    }
                } finally {
                    try {
                        levelDBStore.close();
                    } catch (IOException e) {
                        LOGGER.error("close leveldb failed", e);
                    }
                    dbLock.unlock();
                }
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public void inSchedule(String key, Object value) {
        dbLock.tryLock();
        try {
            try {
                levelDBStore.open();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            levelDBStore.put(key, value);
        } finally {
            try {
                levelDBStore.close();
            } catch (IOException e) {
                LOGGER.error("close leveldb failed", e);
            }
            dbLock.unlock();
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
