package com.lts.kv.iterator;

import com.lts.kv.Entry;
import com.lts.kv.cache.DataCache;
import com.lts.kv.data.DataBlockEngine;
import com.lts.kv.index.IndexItem;
import com.lts.kv.index.MemIndex;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 12/18/15.
 */
public class MemIteratorImpl<K, V> implements DBIterator<Entry<K, V>> {

    private Iterator<Map.Entry<K, IndexItem<K>>> iterator;
    private DataBlockEngine<K, V> dataBlockEngine;
    private DataCache<K, V> dataCache;
    private MemIndex<K, V> index;

    public MemIteratorImpl(MemIndex<K, V> index, DataBlockEngine<K, V> dataBlockEngine, DataCache<K, V> dataCache) {
        this.index = index;
        this.dataBlockEngine = dataBlockEngine;
        this.dataCache = dataCache;
        this.iterator = index.getIndexMap().entrySet().iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Entry<K, V> next() {
        Map.Entry<K, IndexItem<K>> entry = iterator.next();
        IndexItem<K> index = entry.getValue();

        // 1. 从缓存中读取
        V value = dataCache.get(index.getKey());
        if (value != null) {
            return new Entry<K, V>(entry.getKey(), value);
        }
        // 2. 从文件中读取
        value = dataBlockEngine.getValue(index);
        if (value == null) {
            return null;
        }
        return new Entry<K, V>(entry.getKey(), value);
    }

}
