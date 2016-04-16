package com.github.ltsopensource.kv.cache;

/**
 * @author Robert HG (254963746@qq.com) on 12/18/15.
 */
public interface DataCache<K, V> {

    void put(K key, V value);

    V get(K key);

    V remove(K key);

    int size();

    void clear();
}
