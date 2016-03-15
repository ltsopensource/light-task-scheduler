package com.lts.jvmmonitor;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Robert HG (254963746@qq.com) on 3/14/16.
 */
public class JVMMonitorReferenceCount implements ReferenceCount {

    // 这里必须为static, 保证所有实例引用的都是一个REF_COUNT
    private static final AtomicLong REF_COUNT = new AtomicLong(0);

    @Override
    public long incrementAndGet() {
        return REF_COUNT.incrementAndGet();
    }

    @Override
    public long decrementAndGet() {
        return REF_COUNT.decrementAndGet();
    }

    @Override
    public long getCurRefCount() {
        return REF_COUNT.get();
    }
}
