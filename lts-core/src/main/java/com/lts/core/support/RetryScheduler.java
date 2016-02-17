package com.lts.core.support;

import com.lts.core.AppContext;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.GenericsUtils;
import com.lts.core.constant.EcTopic;
import com.lts.core.domain.KVPair;
import com.lts.core.failstore.AbstractFailStore;
import com.lts.core.failstore.FailStore;
import com.lts.core.failstore.FailStoreException;
import com.lts.core.failstore.FailStoreFactory;
import com.lts.core.json.JSON;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.spi.ServiceLoader;
import com.lts.ec.EventInfo;
import com.lts.ec.EventSubscriber;
import com.lts.ec.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 *         重试定时器 (用来发送 给 客户端的反馈信息等)
 */
public abstract class RetryScheduler<T> {

    public static final Logger LOGGER = LoggerFactory.getLogger(RetryScheduler.class);

    private Class<?> type = GenericsUtils.getSuperClassGenericType(this.getClass());

    // 定时检查是否有 师表的反馈任务信息(给客户端的)
    private ScheduledExecutorService RETRY_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService MASTER_RETRY_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> masterScheduledFuture;
    private ScheduledFuture<?> scheduledFuture;
    private AtomicBoolean selfCheckStart = new AtomicBoolean(false);
    private AtomicBoolean masterCheckStart = new AtomicBoolean(false);
    private FailStore failStore;
    // 名称主要是用来记录日志
    private String name;

    // 批量发送的消息数
    private int batchSize = 5;

    private ReentrantLock lock = new ReentrantLock();

    public RetryScheduler(AppContext appContext) {
        this(appContext, appContext.getConfig().getFailStorePath());
    }

    public RetryScheduler(AppContext appContext, String storePath) {
        FailStoreFactory failStoreFactory = ServiceLoader.load(FailStoreFactory.class, appContext.getConfig());
        failStore = failStoreFactory.getFailStore(appContext.getConfig(), storePath);
        try {
            failStore.open();
        } catch (FailStoreException e) {
            throw new RuntimeException(e);
        }
        EventSubscriber subscriber = new EventSubscriber(RetryScheduler.class.getSimpleName()
                .concat(appContext.getConfig().getIdentity()),
                new Observer() {
                    @Override
                    public void onObserved(EventInfo eventInfo) {
                        Boolean isMaster = (Boolean) eventInfo.getParam("isMaster");
                        if (isMaster != null && isMaster) {
                            startMasterCheck();
                        } else {
                            stopMasterCheck();
                        }
                    }
                });
        appContext.getEventCenter().subscribe(subscriber, EcTopic.MASTER_CHANGED);

        if (appContext.getMasterElector().isCurrentMaster()) {
            startMasterCheck();
        }
    }

    public RetryScheduler(AppContext appContext, String storePath, int batchSize) {
        this(appContext, storePath);
        this.batchSize = batchSize;
    }

    protected RetryScheduler(AppContext appContext, int batchSize) {
        this(appContext);
        this.batchSize = batchSize;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void start() {
        try {
            if (selfCheckStart.compareAndSet(false, true)) {
                // 这个时间后面再去优化
                scheduledFuture = RETRY_EXECUTOR_SERVICE.scheduleWithFixedDelay
                        (new CheckSelfRunner(), 10, 30, TimeUnit.SECONDS);
                LOGGER.info("Start {} RetryScheduler success", name);
            }
        } catch (Throwable t) {
            LOGGER.error("Start {} RetryScheduler failed", name, t);
        }
    }

    private void startMasterCheck() {
        try {
            if (masterCheckStart.compareAndSet(false, true)) {
                // 这个时间后面再去优化
                masterScheduledFuture = MASTER_RETRY_EXECUTOR_SERVICE.
                        scheduleWithFixedDelay(new CheckDeadFailStoreRunner(), 30, 60, TimeUnit.SECONDS);
                LOGGER.info("Start {} master RetryScheduler success", name);
            }
        } catch (Throwable t) {
            LOGGER.error("Start {} master RetryScheduler failed.", name, t);
        }
    }

    private void stopMasterCheck() {
        try {
            if (masterCheckStart.compareAndSet(true, false)) {
                masterScheduledFuture.cancel(true);
                MASTER_RETRY_EXECUTOR_SERVICE.shutdown();
                LOGGER.info("Stop {} master RetryScheduler success", name);
            }
        } catch (Throwable t) {
            LOGGER.error("Stop {} master RetryScheduler failed", name, t);
        }
    }

    public void stop() {
        try {
            if (selfCheckStart.compareAndSet(true, false)) {
                scheduledFuture.cancel(true);
                failStore.close();
                RETRY_EXECUTOR_SERVICE.shutdown();
                LOGGER.info("Stop {} RetryScheduler success", name);
            }
        } catch (Throwable t) {
            LOGGER.error("Stop {} RetryScheduler failed", name, t);
        }
    }

    public void destroy() {
        try {
            stop();
            failStore.destroy();
        } catch (FailStoreException e) {
            LOGGER.error("destroy {} RetryScheduler failed", name, e);
        }
    }

    /**
     * 定时检查 提交失败任务的Runnable
     */
    private class CheckSelfRunner implements Runnable {

        @Override
        public void run() {
            try {
                // 1. 检测 远程连接 是否可用
                if (!isRemotingEnable()) {
                    return;
                }

                List<KVPair<String, T>> kvPairs = null;
                do {
                    try {
                        lock.tryLock(1000, TimeUnit.MILLISECONDS);
                        kvPairs = failStore.fetchTop(batchSize, type);

                        if (CollectionUtils.isEmpty(kvPairs)) {
                            break;
                        }

                        List<T> values = new ArrayList<T>(kvPairs.size());
                        List<String> keys = new ArrayList<String>(kvPairs.size());
                        for (KVPair<String, T> kvPair : kvPairs) {
                            keys.add(kvPair.getKey());
                            values.add(kvPair.getValue());
                        }
                        if (retry(values)) {
                            LOGGER.info("{} RetryScheduler, local files send success, size: {}, {}", name, values.size(), JSON.toJSONString(values));
                            failStore.delete(keys);
                        } else {
                            break;
                        }
                    }finally {
                        if(lock.isHeldByCurrentThread()){
                            lock.unlock();
                        }
                    }
                } while (CollectionUtils.isNotEmpty(kvPairs));

            } catch (Throwable e) {
                LOGGER.error("Run {} RetryScheduler error ", name, e);
            }
        }
    }

    /**
     * 定时检查 已经down掉的机器的FailStore目录
     */
    private class CheckDeadFailStoreRunner implements Runnable {

        @Override
        public void run() {
            try {
                // 1. 检测 远程连接 是否可用
                if (!isRemotingEnable()) {
                    return;
                }
                List<FailStore> failStores = null;
                if (failStore instanceof AbstractFailStore) {
                    failStores = ((AbstractFailStore) failStore).getDeadFailStores();
                }
                if (CollectionUtils.isNotEmpty(failStores)) {
                    for (FailStore store : failStores) {
                        store.open();

                        while (true) {
                            List<KVPair<String, T>> kvPairs = store.fetchTop(batchSize, type);
                            if (CollectionUtils.isEmpty(kvPairs)) {
                                store.destroy();
                                LOGGER.info("{} RetryScheduler, delete store dir[{}] success.", name, store.getPath());
                                break;
                            }
                            List<T> values = new ArrayList<T>(kvPairs.size());
                            List<String> keys = new ArrayList<String>(kvPairs.size());
                            for (KVPair<String, T> kvPair : kvPairs) {
                                keys.add(kvPair.getKey());
                                values.add(kvPair.getValue());
                            }
                            if (retry(values)) {
                                LOGGER.info("{} RetryScheduler, dead local files send success, size: {}, {}", name, values.size(), JSON.toJSONString(values));
                                store.delete(keys);
                            } else {
                                store.close();
                                break;
                            }
                            try {
                                Thread.sleep(500);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                LOGGER.error("Run {} master RetryScheduler error.", name, e);
            }
        }
    }

    public void inSchedule(String key, T value) {
        try {
            lock.tryLock();
            failStore.put(key, value);
            LOGGER.info("{} RetryScheduler, local files save success, {}", name, JSON.toJSONString(value));
        } catch (FailStoreException e) {
            LOGGER.error("{} RetryScheduler in schedule error. ", name, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    /**
     * 远程连接是否可用
     */
    protected abstract boolean isRemotingEnable();

    /**
     * 重试
     */
    protected abstract boolean retry(List<T> list);

}
