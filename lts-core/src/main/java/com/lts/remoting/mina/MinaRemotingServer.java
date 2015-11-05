package com.lts.remoting.mina;

import com.lts.remoting.*;
import com.lts.remoting.common.RemotingHelper;
import com.lts.remoting.exception.RemotingException;
import com.lts.remoting.protocol.RemotingCommand;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.transport.socket.nio.NioProcessor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author Robert HG (254963746@qq.com) on 11/4/15.
 */
public class MinaRemotingServer extends AbstractRemotingServer {

    private IoAcceptor acceptor;
    private InetSocketAddress bindAddress;

    public MinaRemotingServer(RemotingServerConfig remotingServerConfig) {
        this(remotingServerConfig, null);
    }

    public MinaRemotingServer(RemotingServerConfig remotingServerConfig, ChannelEventListener channelEventListener) {
        super(remotingServerConfig, channelEventListener);
    }

    @Override
    protected void serverStart() throws RemotingException {
        int executor;
        acceptor = new NioSocketAcceptor(); //TCP Acceptor

//        acceptor.getFilterChain().addFirst("logging", new MinaLoggingFilter());
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MinaCodecFactory(getCodec())));
        acceptor.getFilterChain().addLast("mdc", new MdcInjectionFilter());

        acceptor.setHandler(new MinaHandler());
        IoSessionConfig cfg = acceptor.getSessionConfig();
        cfg.setReaderIdleTime(remotingServerConfig.getReaderIdleTimeSeconds());
        cfg.setWriterIdleTime(remotingServerConfig.getWriterIdleTimeSeconds());
        cfg.setBothIdleTime(remotingServerConfig.getServerChannelMaxIdleTimeSeconds());

        bindAddress = new InetSocketAddress(remotingServerConfig.getListenPort());
        try {
            acceptor.bind(bindAddress);
        } catch (IOException e) {
            throw new RemotingException("Start Mina server error", e);
        }
    }

    @Override
    protected void serverShutdown() {
        if (acceptor != null) {
            acceptor.unbind(bindAddress);
            acceptor.dispose();
        }
    }

    class MinaHandler extends IoHandlerAdapter {

        @Override
        public void sessionCreated(IoSession session) throws Exception {
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(new MinaChannel(session));
            LOGGER.info("SERVER : sessionCreated {}", remoteAddress);
            super.sessionCreated(session);
        }

        @Override
        public void sessionOpened(IoSession session) throws Exception {
            Channel channel = new MinaChannel(session);
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(channel);
            LOGGER.info("SERVER: sessionOpened, the channel[{}]", remoteAddress);

            if (channelEventListener != null) {
                putRemotingEvent(new RemotingEvent(RemotingEventType.CONNECT, remoteAddress, channel));
            }
        }

        @Override
        public void sessionClosed(IoSession session) throws Exception {
            com.lts.remoting.Channel channel = new MinaChannel(session);

            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(channel);
            LOGGER.info("SERVER: sessionClosed, the channel[{}]", remoteAddress);

            if (channelEventListener != null) {
                putRemotingEvent(new RemotingEvent(RemotingEventType.CLOSE, remoteAddress, channel));
            }
        }

        @Override
        public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
            com.lts.remoting.Channel channel = new MinaChannel(session);

            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(channel);

            if (IdleStatus.BOTH_IDLE == status) {
                LOGGER.warn("SERVER: IDLE [{}]", remoteAddress);
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
            LOGGER.warn("SERVER: exceptionCaught {}", remoteAddress);
            LOGGER.warn("SERVER: exceptionCaught exception.", cause);

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
            LOGGER.warn("SERVER: messageSent {}", message);
        }

        @Override
        public void inputClosed(IoSession session) throws Exception {
            LOGGER.warn("SERVER: inputClosed");
            session.close(true);
        }
    }
}
