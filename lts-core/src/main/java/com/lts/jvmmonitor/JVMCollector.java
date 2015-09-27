package com.lts.jvmmonitor;

import com.lts.core.commons.utils.JSONUtils;
import com.lts.jvmmonitor.mbean.JVMGC;
import com.lts.jvmmonitor.mbean.JVMGCMBean;
import com.lts.jvmmonitor.mbean.JVMInfo;
import com.lts.jvmmonitor.mbean.JVMMemory;

import java.util.Arrays;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 9/15/15.
 */
public class JVMCollector {

    private static final JVMInfo JVM_INFO = JVMInfo.getInstance();

    /**
     * 收集信息
     */
    public static void collect() {
        // memory
        Map<String, Object> memoryMap = JVMMonitor.getAttribute(MConstants.JMX_JVM_MEMORY_NAME, Arrays.asList("HeapMemoryInit", "HeapMemoryMax", "HeapMemoryUsed",
                "NonHeapMemoryInit", "NonHeapMemoryMax", "NonHeapMemoryUsed",
                "PermGenMax", "PermGenUsed", "OldGenMax", "OldGenUsed",
                "EdenSpaceMax", "EdenSpaceUsed", "SurvivorMax", "SurvivorUsed"));
        System.out.println("------------memoryMap----------------");
        System.out.println(JSONUtils.toJSONString(memoryMap));

        // gc
        Map<String, Object> gcMap = JVMMonitor.getAttribute(MConstants.JMX_JVM_GC_NAME, Arrays.asList("YoungGCCollectionCount", "YoungGCCollectionTime",
                "FullGCCollectionCount", "FullGCCollectionTime",
                "SpanYoungGCCollectionCount", "SpanYoungGCCollectionTime",
                "SpanFullGCCollectionCount", "SpanFullGCCollectionTime"));
        System.out.println("------------gcMap----------------");
        System.out.println(JSONUtils.toJSONString(gcMap));

        // thread
        Map<String, Object> threadMap = JVMMonitor.getAttribute(MConstants.JMX_JVM_THREAD_NAME, Arrays.asList("DaemonThreadCount", "ThreadCount", "TotalStartedThreadCount",
                "DeadLockedThreadCount", "ProcessCpuTimeRate"));
        System.out.println("------------threadMap----------------");
        System.out.println(JSONUtils.toJSONString(threadMap));

        System.out.println("---------------------------");
    }

    public static void main(String[] args) {
//        JVMMonitor.start();

        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
//                    JVMCollector.collect();

                    System.out.println(JVMGC.getInstance().getFullGCCollectionCount());
                    System.out.println(JVMMemory.getInstance().getEdenSpaceCommitted());
                    System.out.println(JVMMemory.getInstance().getNonHeapMemoryUsed());

                    try {
                        Thread.sleep(10000L);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }).start();
    }

}
