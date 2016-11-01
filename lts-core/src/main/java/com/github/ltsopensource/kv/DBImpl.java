package com.github.ltsopensource.kv;

import com.github.ltsopensource.core.commons.file.FileUtils;
import com.github.ltsopensource.kv.cache.DataCache;
import com.github.ltsopensource.kv.cache.LRUDataCache;
import com.github.ltsopensource.kv.data.DataAppendResult;
import com.github.ltsopensource.kv.data.DataBlockEngine;
import com.github.ltsopensource.kv.index.*;
import com.github.ltsopensource.kv.iterator.DBIterator;
import com.github.ltsopensource.kv.replay.TxLogReplay;
import com.github.ltsopensource.kv.serializer.StoreSerializer;
import com.github.ltsopensource.kv.txlog.StoreTxLogEngine;
import com.github.ltsopensource.kv.txlog.StoreTxLogPosition;

import java.io.Closeable;

/**
 * ------- PUT(REMOVE) --------
 * 1. 一次 事务日志 (TxLog) 的顺序写入
 * 2. 一次 数据文件 (Data) 的顺序写入
 * 3. 一次 内存索引 (Index) 的写入  (后面实现B+树)
 * 4. 数据缓存
 * <p/>
 * ------- GET --------
 * 1. 缓存数据中 GET 命中即返回
 * 2. 从数据文件 (DATA) 中GET
 *
 * @author Robert HG (254963746@qq.com) on 12/13/15.
 */
public class DBImpl<K, V> implements DB<K, V>, Closeable {

    private StoreTxLogEngine<K, V> storeTxLogEngine;
    private DataBlockEngine<K, V> dataBlockEngine;
    private StoreConfig storeConfig;
    private Index<K, V> index;
    private DataCache<K, V> dataCache;
    private IndexSnapshot<K, V> indexSnapshot;
    private TxLogReplay<K, V> txLogReplay;

    public DBImpl(StoreSerializer serializer, StoreConfig storeConfig) {
        this.storeConfig = storeConfig;
        this.dataCache = new LRUDataCache<K, V>(storeConfig.getMaxDataCacheSize());
        this.storeTxLogEngine = new StoreTxLogEngine<K, V>(serializer, storeConfig);
        this.dataBlockEngine = new DataBlockEngine<K, V>(serializer, storeConfig);

        if (IndexType.MEM == storeConfig.getIndexType()) {
            this.index = new MemIndex<K, V>(storeConfig, dataBlockEngine, dataCache);
            this.txLogReplay = new TxLogReplay<K, V>(storeTxLogEngine, dataBlockEngine, index, dataCache);
            this.indexSnapshot = new MemIndexSnapshot<K, V>(txLogReplay, index, storeConfig, serializer);
            ((MemIndex<K, V>) this.index).setIndexSnapshot(this.indexSnapshot);
        } else {
            throw new IllegalArgumentException("Illegal IndexEngine " + storeConfig.getIndexType());
        }
    }

    public void init() throws DBException {
        try {
            FileUtils.createDirIfNotExist(storeConfig.getDbPath());

            storeTxLogEngine.init();
            dataBlockEngine.init();
            indexSnapshot.init();

        } catch (Exception e) {
            throw new DBException("DB init error:" + e.getMessage(), e);
        }
    }

    public int size() {
        return index.size();
    }

    public boolean containsKey(K key) {
        return index.containsKey(key);
    }

    public V get(K key) {
        // 1. 从缓存中获取
        V value = dataCache.get(key);
        if (value != null) {
            return value;
        }

        IndexItem<K> indexItem = index.getIndexItem(key);
        if (indexItem == null) {
            return null;
        }
        // 2. 从Data文件读取
        return dataBlockEngine.getValue(indexItem);
    }

    public void put(K key, V value) {

        // 1. 先写Log
        StoreTxLogPosition storeTxLogPosition = storeTxLogEngine.append(Operation.PUT, key, value);

        // 2. 写Data
        DataAppendResult dataAppendResult = dataBlockEngine.append(storeTxLogPosition, key, value);

        // 3. 写Index
        index.putIndexItem(storeTxLogPosition, key, convertToIndex(key, dataAppendResult));

        // 4. 写缓存
        dataCache.put(key, value);
    }

    public void remove(K key) {
        // 先移除缓存
        dataCache.remove(key);

        // 1. 先写Log
        StoreTxLogPosition storeTxLogPosition = storeTxLogEngine.append(Operation.REMOVE, key);

        // 2. 移除Index
        IndexItem<K> indexItem = index.removeIndexItem(storeTxLogPosition, key);

        if (indexItem != null) {
            // 3. 移除Data
            dataBlockEngine.remove(storeTxLogPosition, indexItem);
        }
    }

    public DBIterator<Entry<K, V>> iterator() {
        return index.iterator();
    }

    @Override
    public void close() {
        dataCache.clear();
    }

    public static <K> IndexItem<K> convertToIndex(K key, DataAppendResult result) {
        IndexItem<K> index = new IndexItem<K>();
        index.setKey(key);
        index.setFileId(result.getFileId());
        index.setFromIndex(result.getFromIndex());
        index.setLength(result.getLength());
        return index;
    }

}
