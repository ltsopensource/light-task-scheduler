package com.lts.core.protocol.command;

/**
 * Created by hugui.hg on 3/27/16.
 */
public class DepJobSubmitResponse extends AbstractRemotingCommandBody {

    private static final long serialVersionUID = 9133108871954698698L;

    private Boolean success = true;

    private String msg;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}