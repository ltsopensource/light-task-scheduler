package com.github.ltsopensource.remoting;

/**
 * 事件
 */
public class RemotingEvent {

    private final RemotingEventType type;
    private final String remoteAddr;
    private final Channel channel;

    public RemotingEvent(RemotingEventType type, String remoteAddr, Channel channel) {
        this.type = type;
        this.remoteAddr = remoteAddr;
        this.channel = channel;
    }

    public RemotingEventType getType() {
        return type;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "RemotingEvent [type=" + type + ", remoteAddr=" + remoteAddr + ", channel=" + channel + "]";
    }
}
