package com.github.ltsopensource.kv.data;

import java.io.Serializable;

/**
 * @author Robert HG (254963746@qq.com) on 12/15/15.
 */
public class DataEntry<K, V> implements Serializable {

    private K key;

    private V value;

    public DataEntry() {
    }

    public DataEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
