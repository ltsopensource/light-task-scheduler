package com.lts.remoting.lts;

import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.nio.channel.NioChannel;
import com.lts.nio.idle.IdleState;
import com.lts.remoting.AbstractRemoting;
import com.lts.remoting.Channel;
import com.lts.remoting.RemotingEvent;
import com.lts.remoting.RemotingEventType;
import com.lts.remoting.common.RemotingHelper;
import com.lts.remoting.protocol.RemotingCommand;

import static com.lts.nio.idle.IdleState.BOTH_IDLE;

/**
 * @author Robert HG (254963746@qq.com) on 2/8/16.
 */
public class LtsEventHandler implements com.lts.nio.handler.NioHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemotingHelper.RemotingLogName);

    private AbstractRemoting remoting;
    private String sideType;      // SERVER , CLIENT

    public LtsEventHandler(AbstractRemoting remoting, String sideType) {
        this.remoting = remoting;
        this.sideType = sideType;
    }

    @Override
    public void exceptionCaught(NioChannel channel, Exception cause) {
        Channel ch = new LtsChannel(channel);
        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ch);
        LOGGER.warn(sideType + ": exceptionCaught {}", remoteAddress, cause);

        if (remoting.getChannelEventListener() != null) {
            remoting.putRemotingEvent(new RemotingEvent(RemotingEventType.EXCEPTION, remoteAddress, ch));
        }
        RemotingHelper.closeChannel(ch);
    }

    @Override
    public void messageReceived(NioChannel channel, Object msg) throws Exception {
        if (msg != null && msg instanceof RemotingCommand) {
            remoting.processMessageReceived(new LtsChannel(channel), (RemotingCommand) msg);
        }
    }

    @Override
    public void channelConnected(NioChannel channel) {
        Channel ch = new LtsChannel(channel);
        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ch);
        LOGGER.info("{}: channelConnected, the channel[{}]", sideType, remoteAddress);

        if (remoting.getChannelEventListener() != null) {
            remoting.putRemotingEvent(new RemotingEvent(RemotingEventType.CONNECT, remoteAddress, ch));
        }
    }

    @Override
    public void channelIdle(NioChannel channel, IdleState state) {
        if (state == null) {
            return;
        }
        com.lts.remoting.Channel ch = new LtsChannel(channel);

        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ch);

        if (BOTH_IDLE == state) {
            LOGGER.info("{}: IDLE [{}]", sideType, remoteAddress);
            RemotingHelper.closeChannel(ch);
        }

        if (remoting.getChannelEventListener() != null) {
            RemotingEventType remotingEventType = null;
            switch (state) {
                case BOTH_IDLE:
                    remotingEventType = RemotingEventType.ALL_IDLE;
                    break;
                case READER_IDLE:
                    remotingEventType = RemotingEventType.READER_IDLE;
                    break;
                case WRITER_IDLE:
                    remotingEventType = RemotingEventType.WRITER_IDLE;
                    break;
            }
            remoting.putRemotingEvent(new RemotingEvent(remotingEventType, remoteAddress, ch));
        }
    }
}
