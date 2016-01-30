package com.lts.nio.channel;

import java.nio.channels.SelectableChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Robert HG (254963746@qq.com) on 1/30/16.
 */
public class ChannelContainer {

    private final ConcurrentMap<SelectableChannel, NioChannel>
            channels = new ConcurrentHashMap<SelectableChannel, NioChannel>();

    public void addChannel(SelectableChannel javaChannel, NioChannel channel) {
        channels.putIfAbsent(javaChannel, channel);
    }

    public NioChannel getChannel(SelectableChannel javaChannel) {
        return channels.get(javaChannel);
    }

    public void removeChannel(SelectableChannel javaChannel) {
        channels.remove(javaChannel);
    }

    public boolean hasChannel(SelectableChannel javaChannel) {
        return channels.containsKey(javaChannel);
    }

    public void clear() {
        channels.clear();
    }

    public int size() {
        return channels.size();
    }

}
