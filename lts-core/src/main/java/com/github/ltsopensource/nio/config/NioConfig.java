package com.github.ltsopensource.nio.config;

/**
 * @author Robert HG (254963746@qq.com) on 2/15/16.
 */
public class NioConfig {

    private Integer receiveBufferSize;

    private Boolean reuseAddress;

    private Integer backlog;

    private Boolean tcpNoDelay;

    private Boolean keepAlive;

    private Integer ipTos;      // trafficClass

    private Boolean oobInline;

    private Integer soLinger;

    private int idleTimeBoth;

    private int idleTimeWrite;

    private int idleTimeRead;

    private int writeTimeoutInMillis;

    public Integer getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(Integer receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public Boolean getReuseAddress() {
        return reuseAddress;
    }

    public void setReuseAddress(Boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    public Integer getBacklog() {
        return backlog;
    }

    public void setBacklog(Integer backlog) {
        this.backlog = backlog;
    }

    public Boolean getTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(Boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public Boolean getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(Boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public Integer getIpTos() {
        return ipTos;
    }

    public void setIpTos(Integer ipTos) {
        this.ipTos = ipTos;
    }

    public Boolean getOobInline() {
        return oobInline;
    }

    public void setOobInline(Boolean oobInline) {
        this.oobInline = oobInline;
    }

    public Integer getSoLinger() {
        return soLinger;
    }

    public void setSoLinger(Integer soLinger) {
        this.soLinger = soLinger;
    }

    public long getIdleTimeBoth() {
        return idleTimeBoth;
    }

    public void setIdleTimeBoth(int idleTimeBoth) {
        this.idleTimeBoth = idleTimeBoth;
    }

    public int getIdleTimeWrite() {
        return idleTimeWrite;
    }

    public void setIdleTimeWrite(int idleTimeWrite) {
        this.idleTimeWrite = idleTimeWrite;
    }

    public int getIdleTimeRead() {
        return idleTimeRead;
    }

    public void setIdleTimeRead(int idleTimeRead) {
        this.idleTimeRead = idleTimeRead;
    }

    public int getWriteTimeoutInMillis() {
        return writeTimeoutInMillis;
    }

    public void setWriteTimeoutInMillis(int writeTimeoutInMillis) {
        this.writeTimeoutInMillis = writeTimeoutInMillis;
    }
}
