package com.lts.web.repository.domain;

/**
 * @author Robert HG (254963746@qq.com) on 9/27/15.
 */
public class JVMInfoDataPo extends AbstractMonitorDataPo {

    /**
     * 节点详细信息
     */
    private String jvmInfo;

    // transient
    private boolean alive;

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public String getJvmInfo() {
        return jvmInfo;
    }

    public void setJvmInfo(String jvmInfo) {
        this.jvmInfo = jvmInfo;
    }
}
