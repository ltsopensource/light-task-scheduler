package com.github.ltsopensource.kv.index;

import com.github.ltsopensource.core.factory.NamedThreadFactory;
import com.github.ltsopensource.kv.DB;
import com.github.ltsopensource.kv.StoreConfig;
import com.github.ltsopensource.kv.serializer.StoreSerializer;
import com.github.ltsopensource.core.logger.Logger;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 12/16/15.
 */
public abstract class AbstractIndexSnapshot<K, V> implements IndexSnapshot<K, V> {

    protected static final Logger LOGGER = DB.LOGGER;
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> future;
    protected Index<K, V> index;
    protected StoreSerializer serializer;
    protected StoreConfig storeConfig;

    public AbstractIndexSnapshot(Index<K, V> index, StoreConfig storeConfig, StoreSerializer serializer) {
        this.index = index;
        this.storeConfig = storeConfig;
        this.serializer = serializer;
        this.executorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("ltsdb-index-snapshot-service", true));
    }

    public void init() throws IOException {
        // 1. load from disk
        loadFromDisk();

        // 2. replay txLog
        replayTxLog();

        future = this.executorService.scheduleWithFixedDelay(new Runnable() {
                                                                 @Override
                                                                 public void run() {
                                                                     try {
                                                                         snapshot();
                                                                     } catch (Throwable t) {
                                                                         LOGGER.error("MemIndexSnapshot snapshot error:" + t.getMessage(), t);
                                                                     }
                                                                 }
                                                             }, storeConfig.getIndexSnapshotInterval(),
                storeConfig.getIndexSnapshotInterval(), TimeUnit.MILLISECONDS);
    }

    /**
     * 从磁盘中加载snapshot到内存中
     */
    protected abstract void loadFromDisk() throws IOException;

    /**
     * 重放没有持久化的事务日志
     */
    protected abstract void replayTxLog();

}
