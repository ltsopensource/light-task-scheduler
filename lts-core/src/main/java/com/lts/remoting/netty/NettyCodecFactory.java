package com.lts.remoting.netty;

import com.lts.core.constant.Constants;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.remoting.Channel;
import com.lts.remoting.codec.Codec;
import com.lts.remoting.common.RemotingHelper;
import com.lts.remoting.protocol.RemotingCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.ByteBuffer;

/**
 * @author Robert HG (254963746@qq.com) on 11/5/15.
 */
public class NettyCodecFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemotingHelper.RemotingLogName);

    private Codec codec;

    public NettyCodecFactory(Codec codec) {
        this.codec = codec;
    }

    @ChannelHandler.Sharable
    public class NettyEncoder extends MessageToByteEncoder<RemotingCommand> {
        @Override
        public void encode(ChannelHandlerContext ctx, RemotingCommand remotingCommand, ByteBuf out)
                throws Exception {

            if (remotingCommand == null) {
                LOGGER.error("Message is null");
                return;
            }

            try {
                ByteBuffer byteBuffer = codec.encode(remotingCommand);
                out.writeBytes(byteBuffer);
            } catch (Exception e) {
                Channel channel = new NettyChannel(ctx);
                LOGGER.error("encode exception, addr={}, remotingCommand={}", RemotingHelper.parseChannelRemoteAddr(channel), remotingCommand.toString(), e);
                RemotingHelper.closeChannel(channel);
            }
        }
    }

    public class NettyDecoder extends LengthFieldBasedFrameDecoder {

        private static final int FRAME_MAX_LENGTH = Constants.DEFAULT_BUFFER_SIZE;

        public NettyDecoder() {
            super(FRAME_MAX_LENGTH, 0, 4, 0, 4);
        }

        @Override
        public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
            try {
                ByteBuf frame = (ByteBuf) super.decode(ctx, in);
                if (frame == null) {
                    return null;
                }

                byte[] tmpBuf = new byte[frame.capacity()];
                frame.getBytes(0, tmpBuf);
                frame.release();

                ByteBuffer byteBuffer = ByteBuffer.wrap(tmpBuf);
                return codec.decode(byteBuffer);
            } catch (Exception e) {
                Channel channel = new NettyChannel(ctx);
                LOGGER.error("decode exception, {}", RemotingHelper.parseChannelRemoteAddr(channel), e);
                // 这里关闭后， 会在pipeline中产生事件，通过具体的close事件来清理数据结构
                RemotingHelper.closeChannel(channel);
            }

            return null;
        }
    }

    public ChannelHandler getEncoder() {
        return new NettyEncoder();
    }

    public ChannelHandler getDecoder() {
        return new NettyDecoder();
    }
}
