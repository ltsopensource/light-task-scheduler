package com.lts.remoting.protocol;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.lts.remoting.CommandBody;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robert HG (254963746@qq.com)
 *  Remoting模块中，服务器与客户端通过传递RemotingCommand来交互
 */
public class RemotingCommand {

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

    private RemotingCommand() {
    }

    public static RemotingCommand createRequestCommand(int code, CommandBody body) {
        RemotingCommand cmd = new RemotingCommand();
        cmd.setCode(code);
        cmd.body = body;
        return cmd;
    }

    public static RemotingCommand createResponseCommand(int code) {
        return createResponseCommand(code, null, null);
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

    public void setBody(CommandBody body) {
        this.body = body;
    }

    public <T extends CommandBody> T getBody() {
        return (T) body;
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
