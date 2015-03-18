package com.lts.job.remoting.protocol;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.lts.job.remoting.CommandBody;
import com.lts.job.remoting.annotation.NotNull;
import com.lts.job.remoting.exception.RemotingCommandException;
import com.lts.job.remoting.exception.RemotingCommandFieldCheckException;
import com.lts.job.remoting.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author Robert HG (254963746@qq.com)
 *
 * Remoting模块中，服务器与客户端通过传递RemotingCommand来交互
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
 */
public class RemotingCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemotingCommand.class);

    private static final int RPC_TYPE = 0; // 0, REQUEST_COMMAND
    private static final int RPC_ONEWAY = 1; // 1, RPC
    // 1, Oneway
    private static AtomicInteger RequestId = new AtomicInteger(0);
    /**
     * Header 部分
     */
    private int code;
    private int subCode;
    private int version = 0;
    private int opaque = RequestId.getAndIncrement();
    private int flag = 0;
    private String remark;
    /**
     * body
     */
    private transient CommandBody body;

    protected RemotingCommand() {
    }

    public static RemotingCommand createRequestCommand(int code, CommandBody body) {
        RemotingCommand cmd = new RemotingCommand();
        cmd.setCode(code);
        cmd.body = body;
        return cmd;
    }

    public static RemotingCommand createResponseCommand(int code, String remark) {
        return createResponseCommand(code, remark, null);
    }

    public static RemotingCommand createResponseCommand(int code, String remark, CommandBody body) {
        RemotingCommand cmd = new RemotingCommand();
        cmd.markResponseType();
        cmd.setCode(code);
        cmd.setRemark(remark);
        cmd.body = body;
        return cmd;
    }

    public static RemotingCommand createResponseCommand(int code, CommandBody body) {
        return createResponseCommand(code, null, body);
    }

    public static RemotingCommand decode(final byte[] array) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(array);
        return decode(byteBuffer);
    }

    public static RemotingCommand decode(final ByteBuffer byteBuffer) {
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

                CommandBody body = null;
                try {
                    body = (CommandBody) RemotingSerializable.decode(bodyData, Class.forName(new String(bodyClassData)));
                } catch (ClassNotFoundException e) {
                    LOGGER.error(e.getMessage(), e);
                }
                cmd.body = body;
            }
        }

        return cmd;
    }

    public <T> T getBody() {
        return (T) body;
    }

    /**
     * 检查commandBody
     *
     * @return
     */
    public boolean checkCommandBody() throws RemotingCommandFieldCheckException {
        if (body != null) {
            body.checkFields();

            try {
                Field[] fields = ReflectionUtils.findFields(body.getClass());
                for (Field field : fields) {
                    if (!Modifier.isStatic(field.getModifiers())) {

                        Annotation annotation = field.getAnnotation(NotNull.class);

                        field.setAccessible(true);
                        Object value = field.get(body);

                        if (annotation != null && value == null) {
                            throw new RemotingCommandFieldCheckException("the field <" + field.getName() + "> is null");
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RemotingCommandFieldCheckException("check field error !", e);
            }
        }

        return true;
    }

    public ByteBuffer encode() throws RemotingCommandException {

        // 1> header length size
        int length = 4;

        // 2> header data length
        byte[] headerData = RemotingSerializable.encode(this);
        length += headerData.length;

        byte[] bodyData = null;
        byte[] bodyClass = null;
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

    public void markResponseType() {
        int bits = 1 << RPC_TYPE;
        this.flag |= bits;
    }

    @JSONField(serialize = false)
    public boolean isResponseType() {
        int bits = 1 << RPC_TYPE;
        return (this.flag & bits) == bits;
    }


    public void markOnewayRPC() {
        int bits = 1 << RPC_ONEWAY;
        this.flag |= bits;
    }


    @JSONField(serialize = false)
    public boolean isOnewayRPC() {
        int bits = 1 << RPC_ONEWAY;
        return (this.flag & bits) == bits;
    }


    public int getCode() {
        return code;
    }


    public void setCode(int code) {
        this.code = code;
    }


    @JSONField(serialize = false)
    public RemotingCommandType getType() {
        if (this.isResponseType()) {
            return RemotingCommandType.RESPONSE_COMMAND;
        }

        return RemotingCommandType.REQUEST_COMMAND;
    }

    public int getVersion() {
        return version;
    }


    public void setVersion(int version) {
        this.version = version;
    }


    public int getOpaque() {
        return opaque;
    }


    public void setOpaque(int opaque) {
        this.opaque = opaque;
    }


    public int getFlag() {
        return flag;
    }

    public int getSubCode() {
        return subCode;
    }

    public void setSubCode(int subCode) {
        this.subCode = subCode;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }


    public String getRemark() {
        return remark;
    }


    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "RemotingCommand{" +
                "code=" + code +
                ", subCode=" + subCode +
                ", version=" + version +
                ", opaque=" + opaque +
                ", flag=" + flag +
                ", remark='" + remark + '\'' +
                ", body=" + JSON.toJSONString(body) +
                '}';
    }
}
