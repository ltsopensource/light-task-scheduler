package com.github.ltsopensource.remoting.protocol;

import com.github.ltsopensource.core.json.JSON;
import com.github.ltsopensource.remoting.RemotingCommandBody;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robert HG (254963746@qq.com)
 *         Remoting模块中，服务器与客户端通过传递RemotingCommand来交互
 */
public class RemotingCommand implements Serializable{

	private static final long serialVersionUID = -6424506729433386206L;
	private static final AtomicInteger requestId = new AtomicInteger(0);
    /**
     * Header 部分
     */
    private int code;
    private int subCode;
    private int version = 0;
    private int opaque;
    private int flag = 0;
    private String remark;
    private int sid = -1;   // serializableTypeId
    /**
     * body
     */
    private transient RemotingCommandBody body;

    private RemotingCommand() {

    }

    public static RemotingCommand createRequestCommand(int code, RemotingCommandBody body) {
        RemotingCommand cmd = new RemotingCommand();
        cmd.setCode(code);
        cmd.setBody(body);
        cmd.setOpaque(requestId.getAndIncrement());
        return cmd;
    }

    public static RemotingCommand createResponseCommand(int code, String remark, RemotingCommandBody body) {
        RemotingCommand cmd = new RemotingCommand();
        RemotingCommandHelper.markResponseType(cmd);
        cmd.setCode(code);
        cmd.setRemark(remark);
        cmd.setBody(body);
        cmd.setOpaque(requestId.getAndIncrement());
        return cmd;
    }

    public static RemotingCommand createResponseCommand(int code, RemotingCommandBody body) {
        return createResponseCommand(code, null, body);
    }

    public static RemotingCommand createResponseCommand(int code) {
        return createResponseCommand(code, null, null);
    }

    public static RemotingCommand createResponseCommand(int code, String remark) {
        return createResponseCommand(code, remark, null);
    }

    public void setBody(RemotingCommandBody body) {
        this.body = body;
    }

    @SuppressWarnings("unchecked")
	public <T extends RemotingCommandBody> T getBody() {
        return (T) body;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
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

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
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
                ", sid='" + sid + '\'' +
                ", body=" + JSON.toJSONString(body) +
                '}';
    }

}
