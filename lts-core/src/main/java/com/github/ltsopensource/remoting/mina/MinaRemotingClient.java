package com.github.ltsopensource.remoting.mina;

import com.github.ltsopensource.remoting.AbstractRemotingClient;
import com.github.ltsopensource.remoting.ChannelEventListener;
import com.github.ltsopensource.remoting.ChannelFuture;
import com.github.ltsopensource.remoting.RemotingClientConfig;
import com.github.ltsopensource.remoting.exception.RemotingException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.SocketAddress;

/**
 * @author Robert HG (254963746@qq.com) on 11/4/15.
 */
public class MinaRemotingClient extends AbstractRemotingClient {

    private NioSocketConnector connector;

    public MinaRemotingClient(RemotingClientConfig remotingClientConfig) {
        this(remotingClientConfig, null);
    }

    public MinaRemotingClient(RemotingClientConfig remotingClientConfig, ChannelEventListener channelEventListener) {
        super(remotingClientConfig, channelEventListener);
    }

    @Override
    protected void clientStart() throws RemotingException {
        try {
            connector = new NioSocketConnector(); //TCP Connector

            // connector.getFilterChain().addFirst("logging", new MinaLoggingFilter());
            connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MinaCodecFactory(getCodec())));
            connector.getFilterChain().addLast("mdc", new MdcInjectionFilter());

            connector.setHandler(new MinaHandler(this));
            IoSessionConfig cfg = connector.getSessionConfig();
            cfg.setReaderIdleTime(remotingClientConfig.getReaderIdleTimeSeconds());
            cfg.setWriterIdleTime(remotingClientConfig.getWriterIdleTimeSeconds());
            cfg.setBothIdleTime(remotingClientConfig.getClientChannelMaxIdleTimeSeconds());
        } catch (Exception e) {
            throw new RemotingException("Mina Client start error", e);
        }
    }

    @Override
    protected void clientShutdown() {
        if (connector != null) {
            connector.dispose();
        }
    }

    @Override
    protected ChannelFuture connect(SocketAddress socketAddress) {
        ConnectFuture connectFuture = connector.connect(socketAddress);
        return new MinaChannelFuture(connectFuture);
    }
}
