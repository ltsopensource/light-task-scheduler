package com.github.ltsopensource.core.domain;

/**
 * @author Robert HG (254963746@qq.com) on 3/6/15.
 */
public class Pair<Key, Value> {
    private Key key;
    private Value value;

    public Pair(Key key, Value value) {
        this.key = key;
        this.value = value;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }
}

