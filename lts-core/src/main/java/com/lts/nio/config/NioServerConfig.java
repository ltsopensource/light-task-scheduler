package com.lts.nio.config;

/**
 * @author Robert HG (254963746@qq.com) on 1/9/16.
 */
public class NioServerConfig {

    private Integer receiveBufferSize;

    private Boolean reuseAddress;

    private Integer backlog;

    private Boolean tcpNoDelay;

    private Boolean keepAlive;

    private int ipTos;      // trafficClass

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

    public int getIpTos() {
        return ipTos;
    }

    public void setIpTos(int ipTos) {
        this.ipTos = ipTos;
    }
}
