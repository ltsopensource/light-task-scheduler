package com.lts.nio.handler;

import com.lts.nio.channel.NioChannel;
import com.lts.nio.channel.NioTcpChannel;

/**
 * @author Robert HG (254963746@qq.com) on 2/4/16.
 */
public class Futures {

    public static CloseFuture newCloseFuture() {
        return new CloseFuture();
    }

    public static WriteFuture newWriteFuture() {
        return new WriteFuture();
    }

    public static ConnectFuture newConnectFuture() {
        return new ConnectFuture();
    }

    public static class ConnectFuture extends IoFuture {

        private NioTcpChannel channel;

        public void setChannel(NioTcpChannel channel) {
            this.channel = channel;
        }

        public NioChannel channel() {
            return channel;
        }
    }

    public static class CloseFuture extends IoFuture {
    }

    public static class WriteFuture extends IoFuture {
    }
}
