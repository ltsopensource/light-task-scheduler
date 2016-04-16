package com.github.ltsopensource.kv.txlog;

import com.github.ltsopensource.kv.Operation;

import java.io.Serializable;

/**
 * @author Robert HG (254963746@qq.com) on 12/13/15.
 */
public class StoreTxLogEntry<K, V> implements Serializable {

    private Operation op;
    private K key;
    private V value;
    private long timestamp;

    public StoreTxLogEntry() {
    }

    public StoreTxLogEntry(Operation op, K key, long timestamp) {
        this.op = op;
        this.key = key;
        this.timestamp = timestamp;
    }

    public StoreTxLogEntry(Operation op, K key, V value, long timestamp) {
        this.op = op;
        this.key = key;
        this.value = value;
        this.timestamp = timestamp;
    }

    public Operation getOp() {
        return op;
    }

    public void setOp(Operation op) {
        this.op = op;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "op=" + op + ", k=" + key + (value == null ? "" : ", v=" + value) + ", timestamp=" + timestamp;
    }
}
