package com.github.ltsopensource.jvmmonitor;

/**
 * @author Robert HG (254963746@qq.com) on 3/14/16.
 */
public interface ReferenceCount {

    /**
     * 增加引用数量
     */
    long incrementAndGet();

    /**
     * 减少引用数量
     */
    long decrementAndGet();

    /**
     * 获取当前的引用数量
     */
    long getCurRefCount();
}
