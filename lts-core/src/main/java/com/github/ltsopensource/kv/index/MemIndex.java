package com.github.ltsopensource.kv.index;

import com.github.ltsopensource.core.factory.NamedThreadFactory;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.kv.Entry;
import com.github.ltsopensource.kv.StoreConfig;
import com.github.ltsopensource.kv.cache.DataCache;
import com.github.ltsopensource.kv.data.DataBlockEngine;
import com.github.ltsopensource.kv.iterator.DBIterator;
import com.github.ltsopensource.kv.iterator.MemIteratorImpl;
import com.github.ltsopensource.kv.txlog.StoreTxLogPosition;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Robert HG (254963746@qq.com) on 12/16/15.
 */
public class MemIndex<K, V> implements Index<K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemIndex.class);
    private StoreTxLogPosition lastTxLog;
    private ConcurrentMap<K, IndexItem<K>> indexMap;
    private StoreConfig storeConfig;
    private DataBlockEngine<K, V> dataBlockEngine;
    private DataCache<K, V> dataCache;
    private AtomicLong lastSnapshotChangeNum = new AtomicLong(0);
    private AtomicLong currentChangeNum = new AtomicLong(0);
    private IndexSnapshot<K, V> indexSnapshot;

    public MemIndex(final StoreConfig storeConfig, DataBlockEngine<K, V> dataBlockEngine, DataCache<K, V> dataCache) {
        this.indexMap = new ConcurrentSkipListMap<K, IndexItem<K>>();
        this.storeConfig = storeConfig;
        this.dataBlockEngine = dataBlockEngine;
        this.dataCache = dataCache;
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("ltsdb-index-snapshot-check-service", true));
        executorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    // 检查一下当改变的量达到一定量时要snapshot
                    if (currentChangeNum.get() - lastSnapshotChangeNum.get() > storeConfig.getIndexSnapshotThreshold()) {
                        indexSnapshot.snapshot();
                    }
                } catch (Throwable t) {
                    LOGGER.error("SNAPSHOT Error", t);
                }
            }
        }, 3, 2, TimeUnit.SECONDS);
    }

    public IndexItem<K> getIndexItem(K key) {
        return indexMap.get(key);
    }

    @Override
    public IndexItem<K> removeIndexItem(StoreTxLogPosition txLogResult, K key) {
        IndexItem<K> value = indexMap.remove(key);
        this.lastTxLog = txLogResult;
        currentChangeNum.incrementAndGet();
        return value;
    }

    @Override
    public void putIndexItem(StoreTxLogPosition txLogResult, K key, IndexItem<K> indexItem) {
        indexMap.put(key, indexItem);
        this.lastTxLog = txLogResult;
        currentChangeNum.incrementAndGet();
    }

    @Override
    public int size() {
        return indexMap.size();
    }

    @Override
    public boolean containsKey(K key) {
        return indexMap.containsKey(key);
    }

    @Override
    public DBIterator<Entry<K, V>> iterator() {
        return new MemIteratorImpl<K, V>(this, dataBlockEngine, dataCache);
    }

    @Override
    public StoreTxLogPosition lastTxLog() {
        return lastTxLog;
    }

    void setLastTxLog(StoreTxLogPosition lastTxLog) {
        this.lastTxLog = lastTxLog;
    }

    public ConcurrentMap<K, IndexItem<K>> getIndexMap() {
        return indexMap;
    }

    void setIndexMap(ConcurrentMap<K, IndexItem<K>> indexMap) {
        this.indexMap = indexMap;
    }

    public void setIndexSnapshot(IndexSnapshot<K, V> indexSnapshot) {
        this.indexSnapshot = indexSnapshot;
    }
}


