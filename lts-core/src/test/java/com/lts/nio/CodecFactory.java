package com.lts.nio;

import com.lts.core.json.JSON;
import com.lts.core.json.TypeReference;
import com.lts.nio.channel.NioChannel;
import com.lts.nio.codec.Decoder;
import com.lts.nio.codec.Encoder;

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
                return out;
            }
            return null;
        }
    };

    static Decoder decoder = new Decoder() {
        @Override
        public Object decode(ByteBuffer in) throws Exception {

            int length = in.getInt();
            byte[] b = new byte[length];
            in.get(b);
            String json = new String(b, "UTF-8");
            RemotingMsg msg = JSON.parse(json, new TypeReference<RemotingMsg>() {
            });
            return msg;
        }
    };

    public static Encoder getEncoder() {
        return encoder;
    }

    public static Decoder getDecoder() {
        return decoder;
    }
}
