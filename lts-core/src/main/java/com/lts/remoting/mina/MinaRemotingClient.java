package com.lts.remoting.mina;

import com.lts.remoting.*;
import com.lts.remoting.common.RemotingHelper;
import com.lts.remoting.exception.RemotingException;
import com.lts.remoting.protocol.RemotingCommand;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
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

//            connector.getFilterChain().addFirst("logging", new MinaLoggingFilter());
            connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MinaCodecFactory(getCodec())));
            connector.getFilterChain().addLast("mdc", new MdcInjectionFilter());

            connector.setHandler(new MinaHandler());
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

    class MinaHandler extends IoHandlerAdapter {

        @Override
        public void sessionCreated(IoSession session) throws Exception {
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(new MinaChannel(session));
            LOGGER.info("CLIENT : sessionCreated {}", remoteAddress);
            super.sessionCreated(session);
        }

        @Override
        public void sessionOpened(IoSession session) throws Exception {
            Channel channel = new MinaChannel(session);
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(channel);
            LOGGER.info("CLIENT: sessionOpened, the channel[{}]", remoteAddress);

            if (channelEventListener != null) {
                putRemotingEvent(new RemotingEvent(RemotingEventType.CONNECT, remoteAddress, channel));
            }
        }

        @Override
        public void sessionClosed(IoSession session) throws Exception {
            com.lts.remoting.Channel channel = new MinaChannel(session);

            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(channel);
            LOGGER.info("CLIENT: sessionClosed, the channel[{}]", remoteAddress);

            if (channelEventListener != null) {
                putRemotingEvent(new RemotingEvent(RemotingEventType.CLOSE, remoteAddress, channel));
            }
        }

        @Override
        public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
            com.lts.remoting.Channel channel = new MinaChannel(session);

            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(channel);

            if (IdleStatus.BOTH_IDLE == status) {
                LOGGER.warn("CLIENT: IDLE [{}]", remoteAddress);
                RemotingHelper.closeChannel(channel);
            }

            if (channelEventListener != null) {
                RemotingEventType remotingEventType = null;
                if (IdleStatus.BOTH_IDLE == status) {
                    remotingEventType = RemotingEventType.ALL_IDLE;
                } else if (IdleStatus.READER_IDLE == status) {
                    remotingEventType = RemotingEventType.READER_IDLE;
                } else if (IdleStatus.WRITER_IDLE == status) {
                    remotingEventType = RemotingEventType.WRITER_IDLE;
                }
                putRemotingEvent(new RemotingEvent(remotingEventType,
                        remoteAddress, channel));
            }
        }

        @Override
        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
            com.lts.remoting.Channel channel = new MinaChannel(session);

            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(channel);
            LOGGER.warn("CLIENT: exceptionCaught {}", remoteAddress);
            LOGGER.warn("CLIENT: exceptionCaught exception.", cause);

            if (channelEventListener != null) {
                putRemotingEvent(new RemotingEvent(RemotingEventType.EXCEPTION, remoteAddress, channel));
            }

            RemotingHelper.closeChannel(channel);
        }

        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            if (message != null && message instanceof RemotingCommand) {
                processMessageReceived(new MinaChannel(session), (RemotingCommand) message);
            }
        }

        @Override
        public void messageSent(IoSession session, Object message) throws Exception {
            LOGGER.warn("CLIENT: messageSent {}", message);
        }

        @Override
        public void inputClosed(IoSession session) throws Exception {
            LOGGER.warn("CLIENT: inputClosed");
            session.close(true);
        }
    }
}
