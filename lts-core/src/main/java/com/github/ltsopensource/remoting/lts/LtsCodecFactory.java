package com.github.ltsopensource.remoting.lts;

import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.nio.channel.NioChannel;
import com.github.ltsopensource.nio.codec.Decoder;
import com.github.ltsopensource.nio.codec.Encoder;
import com.github.ltsopensource.nio.codec.FrameDecoder;
import com.github.ltsopensource.remoting.Channel;
import com.github.ltsopensource.remoting.codec.Codec;
import com.github.ltsopensource.remoting.common.RemotingHelper;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;

import java.nio.ByteBuffer;

/**
 * @author Robert HG (254963746@qq.com) on 2/8/16.
 */
public class LtsCodecFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(LtsCodecFactory.class);

    private Codec codec;

    public LtsCodecFactory(Codec codec) {
        this.codec = codec;
    }

    public class NioEncoder implements Encoder {

        @Override
        public ByteBuffer encode(NioChannel channel, Object msg) {

            if (msg == null) {
                LOGGER.error("Message is null");
                return null;
            }

            if (msg instanceof RemotingCommand) {
                try {
                    return codec.encode((RemotingCommand) msg);
                } catch (Exception e) {
                    Channel c = new LtsChannel(channel);
                    LOGGER.error("encode exception, addr={}, remotingCommand={}", RemotingHelper.parseChannelRemoteAddr(c), ((RemotingCommand) msg).toString(), e);
                    RemotingHelper.closeChannel(c);
                }
            } else {
                LOGGER.error("Message is not instance of " + RemotingCommand.class.getName());
            }
            return null;
        }
    }

    public class NioDecoder extends FrameDecoder {
        @Override
        protected Object decode(NioChannel ch, byte[] frame) throws Exception {
            ByteBuffer byteBuffer = ByteBuffer.wrap(frame);
            try {
                return codec.decode(byteBuffer);
            } catch (Exception e) {
                Channel channel = new LtsChannel(ch);
                LOGGER.error("decode exception, {}", RemotingHelper.parseChannelRemoteAddr(channel), e);
                RemotingHelper.closeChannel(channel);
                throw e;
            }
        }
    }

    public Encoder getEncoder() {
        return new NioEncoder();
    }

    public Decoder getDecoder() {
        return new NioDecoder();
    }

}
