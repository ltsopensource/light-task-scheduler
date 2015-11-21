package com.lts.jvmmonitor;

import com.lts.core.json.JSON;
import com.lts.core.domain.monitor.JVMMonitorData;
import com.lts.jvmmonitor.mbean.JVMGCMBean;
import com.lts.jvmmonitor.mbean.JVMInfoMBean;
import com.lts.jvmmonitor.mbean.JVMMemoryMBean;
import com.lts.jvmmonitor.mbean.JVMThreadMBean;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 9/15/15.
 */
public class JVMCollector {

    /**
     * 收集信息
     */
    public static JVMMonitorData collect() {

        JVMMonitorData jvmMonitorData = new JVMMonitorData();
        // memory
        Map<String, Object> memoryMap = JVMMonitor.getAttribute(MConstants.JMX_JVM_MEMORY_NAME,
                getAttributeList(JVMMemoryMBean.class));
        jvmMonitorData.setMemoryMap(memoryMap);
        // gc
        Map<String, Object> gcMap = JVMMonitor.getAttribute(MConstants.JMX_JVM_GC_NAME,
                getAttributeList(JVMGCMBean.class));
        jvmMonitorData.setGcMap(gcMap);

        // thread
        Map<String, Object> threadMap = JVMMonitor.getAttribute(MConstants.JMX_JVM_THREAD_NAME,
                getAttributeList(JVMThreadMBean.class));
        jvmMonitorData.setThreadMap(threadMap);

        return jvmMonitorData;
    }

    private static List<String> getAttributeList(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        List<String> attributeList = new ArrayList<String>(methods.length);
        for (Method method : methods) {
            // 去掉 get 前缀
            attributeList.add(method.getName().substring(3));
        }
        return attributeList;
    }

    public static Map<String, Object> getJVMInfo() {
        return JVMMonitor.getAttribute(MConstants.JMX_JVM_INFO_NAME,
                getAttributeList(JVMInfoMBean.class));
    }

    public static void main(String[] args) {
        JVMMonitor.start();

        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    JVMMonitorData jvmMonitorData = JVMCollector.collect();
                    System.out.println(JSON.toJSONString(jvmMonitorData));
                    try {
                        Thread.sleep(10000L);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }).start();
    }

}
