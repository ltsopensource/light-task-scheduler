package com.lts.jvmmonitor;

/**
 * @author Robert HG (254963746@qq.com) on 9/15/15.
 */
public interface MConstants {

    String MSG_GETATTRIBUTE = "GetAttribute";
    String MSG_TS = "TS";
    String MSG_S = "S";
    String MSG_T = "T";
    String MSG_VAL = "VAL";

    String CHARSET = "UTF-8";

    String JMX_JVM_INFO_NAME = "com.lts.jvmmonitor:type=JVMInfo";
    String JMX_JVM_MEMORY_NAME = "com.lts.jvmmonitor:type=JVMMemory";
    String JMX_JVM_GC_NAME = "com.lts.jvmmonitor:type=JVMGC";
    String JMX_JVM_THREAD_NAME = "com.lts.jvmmonitor:type=JVMThread";
}
