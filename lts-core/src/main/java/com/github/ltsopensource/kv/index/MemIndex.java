package com.github.ltsopensource.kv.index;

import com.github.ltsopensource.kv.Entry;
import com.github.ltsopensource.kv.StoreConfig;
import com.github.ltsopensource.kv.cache.DataCache;
import com.github.ltsopensource.kv.data.DataBlockEngine;
import com.github.ltsopensource.kv.iterator.DBIterator;
import com.github.ltsopensource.kv.iterator.MemIteratorImpl;
import com.github.ltsopensource.kv.txlog.StoreTxLogPosition;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author Robert HG (254963746@qq.com) on 12/16/15.
 */
public class MemIndex<K, V> implements Index<K, V> {

    private StoreTxLogPosition lastTxLog;
    private ConcurrentMap<K, IndexItem<K>> indexMap;
    private StoreConfig storeConfig;
    private DataBlockEngine<K, V> dataBlockEngine;
    private DataCache<K, V> dataCache;

    public MemIndex(StoreConfig storeConfig, DataBlockEngine<K, V> dataBlockEngine, DataCache<K, V> dataCache) {
        this.indexMap = new ConcurrentSkipListMap<K, IndexItem<K>>();
        this.storeConfig = storeConfig;
        this.dataBlockEngine = dataBlockEngine;
        this.dataCache = dataCache;
    }

    public IndexItem<K> getIndexItem(K key) {
        return indexMap.get(key);
    }

    @Override
    public IndexItem<K> removeIndexItem(StoreTxLogPosition txLogResult, K key) {
        IndexItem<K> value = indexMap.remove(key);
        this.lastTxLog = txLogResult;
        return value;
    }

    @Override
    public void putIndexItem(StoreTxLogPosition txLogResult, K key, IndexItem<K> indexItem) {
        indexMap.put(key, indexItem);
        this.lastTxLog = txLogResult;
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

}


