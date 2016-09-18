package com.github.ltsopensource.kv;

/**
 * @author Robert HG (254963746@qq.com) on 12/19/15.
 */
public interface Cursor<V> {

    boolean hasNext();

    V next();
}
