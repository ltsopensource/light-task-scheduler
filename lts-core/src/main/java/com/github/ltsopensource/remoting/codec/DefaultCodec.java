package com.github.ltsopensource.remoting.codec;

import com.github.ltsopensource.remoting.RemotingCommandBody;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;
import com.github.ltsopensource.remoting.serialize.RemotingSerializable;

import java.nio.ByteBuffer;

/**
 * @author Robert HG (254963746@qq.com) on 11/5/15.
 *
 * 协议格式
 *    | <length> | <serializable id> | <header length> | <header data> | <body length> | <body data> | <body class> |
 *    |    1     |        2          |        3        |       4       |       5       |      6      |      7       |
 *
 *    1、大端4个字节整数，等于2、3、4、5、6, 7长度总和
 *    1、大端4个字节整数，等 serializable id
 *    3、header 信息长度 大端4个字节整数，等于3的长度
 *    4、header 信息内容
 *    5、body 信息长度  大端4个字节整数，等于5的长度
 *    6、body 信息内容
 *    7、body 的class名称
 */
public class DefaultCodec extends AbstractCodec {

    @Override
    public RemotingCommand decode(ByteBuffer byteBuffer) throws Exception {

        int length = byteBuffer.limit();
        int serializableId = byteBuffer.getInt();

        RemotingSerializable serializable =
                getRemotingSerializable(serializableId);

        int headerLength = byteBuffer.getInt();
        byte[] headerData = new byte[headerLength];
        byteBuffer.get(headerData);

        RemotingCommand cmd = serializable.deserialize(headerData, RemotingCommand.class);

        int remaining = length - 4 - 4 - headerLength;

        if (remaining > 0) {

            int bodyLength = byteBuffer.getInt();
            int bodyClassLength = remaining - 4 - bodyLength;

            if (bodyLength > 0) {

                byte[] bodyData = new byte[bodyLength];
                byteBuffer.get(bodyData);

                byte[] bodyClassData = new byte[bodyClassLength];
                byteBuffer.get(bodyClassData);

                cmd.setBody((RemotingCommandBody) serializable.deserialize(bodyData, Class.forName(new String(bodyClassData))));
            }
        }
        return cmd;
    }

    @Override
    public ByteBuffer encode(RemotingCommand remotingCommand) throws Exception {

        RemotingSerializable serializable =
                getRemotingSerializable(remotingCommand.getSid());

        // header length size
        int length = 4;

        // serializable id (int)
        length += 4;

        //  header data length
        byte[] headerData = serializable.serialize(remotingCommand);
        length += headerData.length;

        byte[] bodyData = null;
        byte[] bodyClass = null;

        RemotingCommandBody body = remotingCommand.getBody();

        if (body != null) {
            // body data
            bodyData = serializable.serialize(body);
            length += bodyData.length;

            bodyClass = body.getClass().getName().getBytes();
            length += bodyClass.length;

            length += 4;
        }

        ByteBuffer result = ByteBuffer.allocate(4 + length);

        // length
        result.putInt(length);

        // serializable Id
        result.putInt(serializable.getId());

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
