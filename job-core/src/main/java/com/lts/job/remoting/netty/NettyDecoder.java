package com.lts.job.remoting.netty;

import com.lts.job.remoting.common.RemotingHelper;
import com.lts.job.remoting.common.RemotingUtil;
import com.lts.job.remoting.protocol.RemotingCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 协议解码器
 */
public class NettyDecoder extends LengthFieldBasedFrameDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemotingHelper.RemotingLogName);
    private static final int FRAME_MAX_LENGTH = 1024 * 1024 * 8;


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

            return RemotingCommand.decode(tmpBuf);
        } catch (Exception e) {
            LOGGER.error("decode exception, {}", RemotingHelper.parseChannelRemoteAddr(ctx.channel()), e);
            // 这里关闭后， 会在pipeline中产生事件，通过具体的close事件来清理数据结构
            RemotingUtil.closeChannel(ctx.channel());
        }

        return null;
    }
}
