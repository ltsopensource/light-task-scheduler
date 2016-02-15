package com.lts.nio.config;

/**
 * Created by hugui.hg on 2/15/16.
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

    private long idleTimeBoth;

    private long idleTimeWrite;

    private long idleTimeRead;

    private long writeTimeoutInMillis;

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

    public void setIdleTimeBoth(long idleTimeBoth) {
        this.idleTimeBoth = idleTimeBoth;
    }

    public long getIdleTimeWrite() {
        return idleTimeWrite;
    }

    public void setIdleTimeWrite(long idleTimeWrite) {
        this.idleTimeWrite = idleTimeWrite;
    }

    public long getIdleTimeRead() {
        return idleTimeRead;
    }

    public void setIdleTimeRead(long idleTimeRead) {
        this.idleTimeRead = idleTimeRead;
    }

    public long getWriteTimeoutInMillis() {
        return writeTimeoutInMillis;
    }

    public void setWriteTimeoutInMillis(long writeTimeoutInMillis) {
        this.writeTimeoutInMillis = writeTimeoutInMillis;
    }
}
