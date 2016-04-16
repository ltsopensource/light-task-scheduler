package com.github.ltsopensource.core.domain.monitor;

import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 9/27/15.
 */
public class JvmMData {

    private Map<String, Object> memoryMap;

    private Map<String, Object> gcMap;

    private Map<String, Object> threadMap;

    public Map<String, Object> getMemoryMap() {
        return memoryMap;
    }

    public void setMemoryMap(Map<String, Object> memoryMap) {
        this.memoryMap = memoryMap;
    }

    public Map<String, Object> getGcMap() {
        return gcMap;
    }

    public void setGcMap(Map<String, Object> gcMap) {
        this.gcMap = gcMap;
    }

    public Map<String, Object> getThreadMap() {
        return threadMap;
    }

    public void setThreadMap(Map<String, Object> threadMap) {
        this.threadMap = threadMap;
    }
}
