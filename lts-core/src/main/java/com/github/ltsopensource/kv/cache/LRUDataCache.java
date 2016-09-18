package com.github.ltsopensource.kv.cache;

import com.github.ltsopensource.core.commons.utils.LRUCache;

import java.util.Collections;
import java.util.Map;

/**
 * 用来缓存
 * @author Robert HG (254963746@qq.com) on 12/18/15.
 */
public class LRUDataCache<K, V> implements DataCache<K, V> {

    private Map<K, V> cache;

    public LRUDataCache(final int maxCacheSize) {
        this.cache = Collections.synchronizedMap(new LRUCache<K, V>(maxCacheSize));
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public V get(K key) {
        return cache.get(key);
    }

    @Override
    public V remove(K key) {
        return cache.remove(key);
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public void clear() {
        cache.clear();
    }

}
