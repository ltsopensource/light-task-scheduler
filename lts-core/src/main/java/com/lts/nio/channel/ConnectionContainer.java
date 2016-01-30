package com.lts.nio.channel;

import java.nio.channels.SelectableChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Robert HG (254963746@qq.com) on 1/30/16.
 */
public class ConnectionContainer {

    private final ConcurrentMap<SelectableChannel, NioConnection>
            connections = new ConcurrentHashMap<SelectableChannel, NioConnection>();

    public void addConnection(SelectableChannel channel, NioConnection connection) {
        connections.putIfAbsent(channel, connection);
    }

    public NioConnection getConnection(SelectableChannel channel) {
        return connections.get(channel);
    }

    public void removeConnection(SelectableChannel channel) {
        connections.remove(channel);
    }

    public boolean hasConnection(SelectableChannel channel) {
        return connections.containsKey(channel);
    }

    public void clear() {
        connections.clear();
    }

    public int size() {
        return connections.size();
    }

}
