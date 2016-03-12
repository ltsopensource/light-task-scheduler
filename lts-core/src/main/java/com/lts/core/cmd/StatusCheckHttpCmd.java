package com.lts.core.cmd;

import com.lts.cmd.HttpCmdProc;
import com.lts.cmd.HttpCmdRequest;
import com.lts.cmd.HttpCmdResponse;
import com.lts.core.cluster.Config;

/**
 * 主要用于启动检测, 通过调用该命令检测是否启动成功
 * @author Robert HG (254963746@qq.com) on 3/10/16.
 */
public class StatusCheckHttpCmd implements HttpCmdProc {

    private Config config;

    public StatusCheckHttpCmd(Config config) {
        this.config = config;
    }

    @Override
    public String nodeIdentity() {
        return config.getIdentity();
    }

    @Override
    public String getCommand() {
        return HttpCmdNames.HTTP_CMD_STATUS_CHECK;
    }

    @Override
    public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {
        HttpCmdResponse response = new HttpCmdResponse();
        response.setSuccess(true);
        response.setMsg("ok");
        return response;
    }
}
