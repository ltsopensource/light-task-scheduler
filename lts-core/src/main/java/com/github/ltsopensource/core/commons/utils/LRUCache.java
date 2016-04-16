package com.github.ltsopensource.core.commons.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 11/9/15.
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = 1L;
    private final int         maxSize;

    public LRUCache(int maxSize){
        this(maxSize, 16, 0.75f, false);
    }

    public LRUCache(int maxSize, int initialCapacity, float loadFactor, boolean accessOrder){
        super(initialCapacity, loadFactor, accessOrder);
        this.maxSize = maxSize;
    }

    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return this.size() > this.maxSize;
    }
}

