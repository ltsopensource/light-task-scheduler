package com.github.ltsopensource.remoting.mina;

import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.remoting.Channel;
import com.github.ltsopensource.remoting.codec.Codec;
import com.github.ltsopensource.remoting.common.RemotingHelper;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.IoBufferAllocator;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.*;

import java.nio.ByteBuffer;

/**
 * @author Robert HG (254963746@qq.com) on 11/4/15.
 */
public class MinaCodecFactory implements ProtocolCodecFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinaCodecFactory.class);

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
            }
        }
    };

    private ProtocolDecoder decoder = new CumulativeProtocolDecoder() {

        @Override
        protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {

            while (in.remaining() > 4) {
                // 前4位是长度
                byte[] lengthBytes = new byte[4];
                in.mark();              //标记当前位置，以便reset
                in.get(lengthBytes);      //读取前4字节
                int length = ByteBuffer.wrap(lengthBytes).getInt();

                // 数据不够，返回false，需要继续读取
                if (length == 0 || length > in.remaining()) {
                    in.reset();
                    return false;
                }

                // 够了，开始解码
                byte[] bytes = new byte[length];

                in.get(bytes, 0, length);

                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                try {
                    RemotingCommand remotingCommand = codec.decode(byteBuffer);
                    out.write(remotingCommand);
                } catch (Exception e) {
                    Channel channel = new MinaChannel(session);
                    LOGGER.error("decode exception, {}", RemotingHelper.parseChannelRemoteAddr(channel), e);
                    RemotingHelper.closeChannel(channel);
                }
            }
            return false;
        }

    };

}
