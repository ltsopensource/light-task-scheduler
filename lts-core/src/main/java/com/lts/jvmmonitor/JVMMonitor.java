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

/**
 * @author Robert HG (254963746@qq.com) on 9/15/15.
 */
public class JVMMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JVMMonitor.class);

    private static final MBeanServer MBEAN_SERVER = ManagementFactory.getPlatformMBeanServer();
    private static final AtomicBoolean start = new AtomicBoolean(false);

    private final static Map<String, Object> MONITOR_MAP = new HashMap<String, Object>();

    public static void start() {
        if (start.compareAndSet(false, true)) {
            if (CollectionUtils.isEmpty(MONITOR_MAP)) {
                MONITOR_MAP.put(MConstants.JMX_JVM_INFO_NAME, JVMInfo.getInstance());
                MONITOR_MAP.put(MConstants.JMX_JVM_MEMORY_NAME, JVMMemory.getInstance());
                MONITOR_MAP.put(MConstants.JMX_JVM_GC_NAME, JVMGC.getInstance());
                MONITOR_MAP.put(MConstants.JMX_JVM_THREAD_NAME, JVMThread.getInstance());
            }
            try {
                for (Map.Entry<String, Object> entry : MONITOR_MAP.entrySet()) {
                    MBEAN_SERVER.registerMBean(entry.getValue(), new ObjectName(entry.getKey()));
                }
                LOGGER.info("Start jvm monitor succeed ");
            } catch (Exception e) {
                LOGGER.error("Start jvm monitor error ", e);
            }
        }
    }

    public static void stop() {
        if (start.compareAndSet(true, false)) {
            for (Map.Entry<String, Object> entry : MONITOR_MAP.entrySet()) {
                try {
                    ObjectName objectName = new ObjectName(entry.getKey());
                    if (MBEAN_SERVER.isRegistered(objectName)) {
                        MBEAN_SERVER.unregisterMBean(objectName);
                    }
                } catch (Exception e) {
                    LOGGER.error("Stop jvm monitor {} error", entry.getKey(), e);
                }
            }
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
            LOGGER.warn(e.getMessage());
        }
        return result;
    }
}
