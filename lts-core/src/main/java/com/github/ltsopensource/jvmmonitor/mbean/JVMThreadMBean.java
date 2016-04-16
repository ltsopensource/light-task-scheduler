package com.github.ltsopensource.jvmmonitor.mbean;

import java.math.BigDecimal;

/**
 * @author Robert HG (254963746@qq.com) on 9/15/15.
 */
public interface JVMThreadMBean {

    int getDaemonThreadCount();

    int getThreadCount();

    long getTotalStartedThreadCount();

    int getDeadLockedThreadCount();

    BigDecimal getProcessCpuTimeRate();
}
