package com.lts.jvmmonitor;

import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.jvmmonitor.mbean.JVMGC;
import com.lts.jvmmonitor.mbean.JVMInfo;
import com.lts.jvmmonitor.mbean.JVMMemory;
import com.lts.jvmmonitor.mbean.JVMThread;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Robert HG (254963746@qq.com) on 9/15/15.
 */
public class JVMMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JVMMonitor.class);

    private static final MBeanServer MBEAN_SERVER = ManagementFactory.getPlatformMBeanServer();
    private static final AtomicBoolean start = new AtomicBoolean(false);
    // 保证多个ClassLoader都是同一个类
    private static final ReferenceCount REF_COUNT = getRefCount();

    private final static Map<String, Object> MONITOR_MAP = new HashMap<String, Object>();

    public static void start() {
        REF_COUNT.incrementAndGet();
        if (start.compareAndSet(false, true)) {
            if (CollectionUtils.isEmpty(MONITOR_MAP)) {
                MONITOR_MAP.put(JVMConstants.JMX_JVM_INFO_NAME, JVMInfo.getInstance());
                MONITOR_MAP.put(JVMConstants.JMX_JVM_MEMORY_NAME, JVMMemory.getInstance());
                MONITOR_MAP.put(JVMConstants.JMX_JVM_GC_NAME, JVMGC.getInstance());
                MONITOR_MAP.put(JVMConstants.JMX_JVM_THREAD_NAME, JVMThread.getInstance());
            }
            try {
                for (Map.Entry<String, Object> entry : MONITOR_MAP.entrySet()) {
                    ObjectName objectName = new ObjectName(entry.getKey());
                    if (!MBEAN_SERVER.isRegistered(objectName)) {
                        MBEAN_SERVER.registerMBean(entry.getValue(), objectName);
                    }
                }
                LOGGER.info("Start JVMMonitor succeed ");
            } catch (Exception e) {
                LOGGER.error("Start JVMMonitor error ", e);
            }
        }
    }

    public static void stop() {
        REF_COUNT.decrementAndGet();
        // 只有启动了,并且引用为0的时候才unregister
        if (start.compareAndSet(true, false) && REF_COUNT.getCurRefCount() == 0) {
            for (Map.Entry<String, Object> entry : MONITOR_MAP.entrySet()) {
                try {
                    ObjectName objectName = new ObjectName(entry.getKey());
                    if (MBEAN_SERVER.isRegistered(objectName)) {
                        MBEAN_SERVER.unregisterMBean(objectName);
                    }
                } catch (Exception e) {
                    LOGGER.error("Stop JVMMonitor {} error", entry.getKey(), e);
                }
            }
            LOGGER.info("Stop JVMMonitor succeed ");
        }
    }

    public static Map<String, Object> getAttribute(String objectName, List<String> attributeNames) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            for (String attributeName : attributeNames) {
                try {
                    Object value = MBEAN_SERVER.getAttribute(new ObjectName(objectName), attributeName);
                    result.put(attributeName, value);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            LOGGER.error("get Attribute error, objectName=" + objectName + ", attributeName=" + attributeNames, e);
        }
        return result;
    }

    public static Object getAttribute(String objectName, String attributeName) {
        try {
            return MBEAN_SERVER.getAttribute(new ObjectName(objectName), attributeName);
        } catch (Exception e) {
            LOGGER.error("get Attribute error, objectName=" + objectName + ", attributeName=" + attributeName, e);
        }
        return null;
    }

    private static ReferenceCount getRefCount() {
        try {
            return (ReferenceCount) loadClass("com.lts.jvmmonitor.JVMMonitorReferenceCount").newInstance();
        } catch (Throwable e) {

            LOGGER.warn("load com.lts.jvmmonitor.JVMMonitorReferenceCount error", e);

            return new ReferenceCount() {
                private final AtomicLong count = new AtomicLong(0);
                public long incrementAndGet() {
                    return count.incrementAndGet();
                }
                public long decrementAndGet() {
                    return count.decrementAndGet();
                }
                public long getCurRefCount() {
                    return count.get();
                }
            };
        }
    }

    private static Class loadClass(String classname) throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        if (classLoader == null)
            classLoader = JVMMonitor.class.getClassLoader();

        return (classLoader.loadClass(classname));
    }

}
