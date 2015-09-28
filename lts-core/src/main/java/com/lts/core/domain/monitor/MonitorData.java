package com.lts.core.domain.monitor;

/**
 * @author Robert HG (254963746@qq.com) on 8/30/15.
 */
public class MonitorData {

    private Long timestamp;

    private JVMMonitorData jvmMonitorData;

    public JVMMonitorData getJvmMonitorData() {
        return jvmMonitorData;
    }

    public void setJvmMonitorData(JVMMonitorData jvmMonitorData) {
        this.jvmMonitorData = jvmMonitorData;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

}
