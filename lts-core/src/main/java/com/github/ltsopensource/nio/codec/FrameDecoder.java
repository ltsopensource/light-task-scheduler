package com.github.ltsopensource.nio.codec;

import com.github.ltsopensource.nio.channel.NioChannel;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 2/16/16.
 */
public abstract class FrameDecoder<T> implements Decoder {

    private static final int MESSAGE_LENGTH_HEAD = 4;

    @Override
    public List<Object> decode(NioChannel channel, ByteBuffer input) throws Exception {

        List<Object> list = new ArrayList<Object>();

        while (input.remaining() > MESSAGE_LENGTH_HEAD) {
            // 前4位是长度
            byte[] lengthBytes = new byte[MESSAGE_LENGTH_HEAD];
            input.mark();              //标记当前位置，以便reset
            input.get(lengthBytes);      //读取前4字节
            int length = ByteBuffer.wrap(lengthBytes).getInt();

            // 数据不够，返回，需要继续读取
            if (length == 0 || length > input.remaining()) {
                input.reset();
                return list;
            }
            // 够了，开始解码
            byte[] bytes = new byte[length];

            input.get(bytes, 0, length);

            T obj = decode(channel, bytes);
            list.add(obj);
        }
        return list;
    }

    protected abstract T decode(NioChannel channel, byte[] frame) throws Exception;

}