package com.github.ltsopensource.nio;

import com.github.ltsopensource.core.json.JSON;
import com.github.ltsopensource.core.json.TypeReference;
import com.github.ltsopensource.nio.channel.NioChannel;
import com.github.ltsopensource.nio.codec.Decoder;
import com.github.ltsopensource.nio.codec.Encoder;
import com.github.ltsopensource.nio.codec.FrameDecoder;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * @author Robert HG (254963746@qq.com) on 2/3/16.
 */
public class CodecFactory {

    static Encoder encoder = new Encoder() {
        @Override
        public ByteBuffer encode(NioChannel channel, Object msg) {

            if (msg instanceof RemotingMsg) {
                String json = JSON.toJSONString(msg);
                if (json == null) {
                    return null;
                }
                int length = json.length();
                ByteBuffer out = ByteBuffer.allocate(4 + length);
                out.putInt(length);
                try {
                    out.put(json.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                out.flip();
                return out;
            }
            return null;
        }
    };

    public static Encoder getEncoder() {
        return encoder;
    }

    public static Decoder getDecoder() {
        return new FrameDecoder() {
            @Override
            protected Object decode(NioChannel channel, byte[] frame) throws Exception {
                String json = new String(frame, "UTF-8");
                RemotingMsg msg = JSON.parse(json, new TypeReference<RemotingMsg>() {
                });
                return msg;
            }
        };
    }
}
