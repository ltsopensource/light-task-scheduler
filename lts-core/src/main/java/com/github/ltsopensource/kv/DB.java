package com.github.ltsopensource.kv;

import com.github.ltsopensource.kv.iterator.DBIterator;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;

/**
 * @author Robert HG (254963746@qq.com) on 12/13/15.
 */
public interface DB<K, V> {

    Logger LOGGER = LoggerFactory.getLogger(DB.class);

    void init() throws DBException;

    int size();

    boolean containsKey(K key);

    V get(K key);

    void put(K key, V value);

    void remove(K key);

    DBIterator<Entry<K, V>> iterator();

    void close();

}
