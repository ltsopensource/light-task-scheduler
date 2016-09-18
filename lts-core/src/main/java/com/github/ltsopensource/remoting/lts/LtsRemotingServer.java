package com.github.ltsopensource.remoting.lts;

import com.github.ltsopensource.nio.NioServer;
import com.github.ltsopensource.nio.channel.ChannelInitializer;
import com.github.ltsopensource.nio.codec.Decoder;
import com.github.ltsopensource.nio.codec.Encoder;
import com.github.ltsopensource.nio.config.NioServerConfig;
import com.github.ltsopensource.remoting.AbstractRemotingServer;
import com.github.ltsopensource.remoting.ChannelEventListener;
import com.github.ltsopensource.remoting.RemotingServerConfig;
import com.github.ltsopensource.remoting.exception.RemotingException;

import java.net.InetSocketAddress;

/**
 * @author Robert HG (254963746@qq.com) on 2/8/16.
 */
public class LtsRemotingServer extends AbstractRemotingServer {

    private NioServer server;

    public LtsRemotingServer(RemotingServerConfig remotingServerConfig, ChannelEventListener channelEventListener) {
        super(remotingServerConfig, channelEventListener);
    }

    public LtsRemotingServer(RemotingServerConfig remotingServerConfig) {
        this(remotingServerConfig, null);
    }

    @Override
    protected void serverStart() throws RemotingException {
        NioServerConfig serverConfig = new NioServerConfig();
        serverConfig.setBacklog(65536);
        serverConfig.setReuseAddress(true);
        serverConfig.setTcpNoDelay(true);

        serverConfig.setIdleTimeBoth(remotingServerConfig.getServerChannelMaxIdleTimeSeconds());
        serverConfig.setIdleTimeWrite(remotingServerConfig.getWriterIdleTimeSeconds());
        serverConfig.setIdleTimeRead(remotingServerConfig.getReaderIdleTimeSeconds());

        final LtsCodecFactory codecFactory = new LtsCodecFactory(getCodec());

        server = new NioServer(serverConfig, new LtsEventHandler(this, "SERVER"), new ChannelInitializer() {
            @Override
            protected Decoder getDecoder() {
                return codecFactory.getDecoder();
            }

            @Override
            protected Encoder getEncoder() {
                return codecFactory.getEncoder();
            }
        });

        server.bind(new InetSocketAddress(this.remotingServerConfig.getListenPort()));
    }

    @Override
    protected void serverShutdown() throws RemotingException {
        server.shutdownGracefully();
    }

}
