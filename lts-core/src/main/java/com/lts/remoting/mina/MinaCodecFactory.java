package com.lts.remoting.mina;

import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.remoting.Channel;
import com.lts.remoting.codec.Codec;
import com.lts.remoting.common.RemotingHelper;
import com.lts.remoting.protocol.RemotingCommand;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.IoBufferAllocator;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Robert HG (254963746@qq.com) on 11/4/15.
 */
public class MinaCodecFactory implements ProtocolCodecFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemotingHelper.RemotingLogName);

    private Codec codec;

    public MinaCodecFactory(Codec codec) {
        this.codec = codec;
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return encoder;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return decoder;
    }

    IoBufferAllocator bufferAllocator = new SimpleBufferAllocator();

    private ProtocolEncoder encoder = new ProtocolEncoderAdapter() {

        @Override
        public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
            if (message == null) {
                LOGGER.error("Message is null");
                return;
            }
            if (!(message instanceof RemotingCommand)) {
                LOGGER.error("{} is not instanceof RemotingCommand", message);
                return;
            }
            RemotingCommand remotingCommand = (RemotingCommand) message;

            try {
                ByteBuffer byteBuffer = codec.encode(remotingCommand);
                IoBuffer ioBuffer = bufferAllocator.wrap(byteBuffer);
                out.write(ioBuffer);
                out.flush();
            } catch (Exception e) {
                Channel channel = new MinaChannel(session);
                LOGGER.error("encode exception, addr={}, remotingCommand={}", RemotingHelper.parseChannelRemoteAddr(channel), remotingCommand.toString(), e);
                RemotingHelper.closeChannel(channel);
                throw e;
            }
        }
    };

    private ProtocolDecoder decoder = new CumulativeProtocolDecoder() {

        @Override
        protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
            try {
                // TODO 
                in.order(ByteOrder.BIG_ENDIAN);
                RemotingCommand remotingCommand = codec.decode(in.buf());
                out.write(remotingCommand);
            } catch (Exception e) {
                Channel channel = new MinaChannel(session);
                LOGGER.error("decode exception, {}", RemotingHelper.parseChannelRemoteAddr(channel), e);
                RemotingHelper.closeChannel(channel);
                throw e;
            }
            return true;
        }
    };

}
