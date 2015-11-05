package com.lts.remoting.codec;

import com.lts.remoting.CommandBody;
import com.lts.remoting.protocol.RemotingCommand;
import com.lts.remoting.protocol.RemotingSerializable;

import java.nio.ByteBuffer;

/**
 * @author Robert HG (254963746@qq.com) on 11/5/15.
 * <p/>
 * // Remoting通信协议
 * //
 * // 协议格式 <length> <header length> <header data> <body length> <body data> <body class>
 * //            1        2               3             4             5             6
 * // 协议分4部分，含义分别如下
 * //     1、大端4个字节整数，等于2、3、4、5、6长度总和
 * //     2、header 信息长度 大端4个字节整数，等于3的长度
 * //     3、header 信息内容
 * //     4、body 信息长度  大端4个字节整数，等于5的长度
 * //     5、body 信息内容
 * //     6、body 的class名称
 * </p>
 */
public class DefaultCodec implements Codec {

    @Override
    public RemotingCommand decode(ByteBuffer byteBuffer) throws Exception {

        int length = byteBuffer.limit();
        int headerLength = byteBuffer.getInt();
        byte[] headerData = new byte[headerLength];
        byteBuffer.get(headerData);

        RemotingCommand cmd = RemotingSerializable.decode(headerData, RemotingCommand.class);

        if (length - 4 - headerLength > 0) {
            int bodyLength = byteBuffer.getInt();
            int bodyClassLength = length - 4 - headerLength - 4 - bodyLength;

            if (bodyLength > 0) {

                byte[] bodyData = new byte[bodyLength];
                byteBuffer.get(bodyData);

                byte[] bodyClassData = new byte[bodyClassLength];
                byteBuffer.get(bodyClassData);

                cmd.setBody((CommandBody) RemotingSerializable.decode(bodyData, Class.forName(new String(bodyClassData))));
            }
        }
        return cmd;
    }

    @Override
    public ByteBuffer encode(RemotingCommand remotingCommand) {

        // 1> header length size
        int length = 4;

        // 2> header data length
        byte[] headerData = RemotingSerializable.encode(remotingCommand);
        length += headerData.length;

        byte[] bodyData = null;
        byte[] bodyClass = null;

        CommandBody body = remotingCommand.getBody();

        if (body != null) {
            // body data
            bodyData = RemotingSerializable.encode(body);
            length += bodyData.length;

            bodyClass = body.getClass().getName().getBytes();
            length += bodyClass.length;

            length += 4;
        }

        ByteBuffer result = ByteBuffer.allocate(4 + length);

        // length
        result.putInt(length);

        // header length
        result.putInt(headerData.length);

        // header data
        result.put(headerData);

        if (bodyData != null) {
            //  body length
            result.putInt(bodyData.length);
            //  body data
            result.put(bodyData);
            // body class
            result.put(bodyClass);
        }

        result.flip();

        return result;
    }
}
