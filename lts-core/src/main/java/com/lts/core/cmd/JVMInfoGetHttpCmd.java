package com.lts.core.cmd;

import com.lts.cmd.HttpCmdProc;
import com.lts.cmd.HttpCmdRequest;
import com.lts.cmd.HttpCmdResponse;
import com.lts.core.cluster.Config;
import com.lts.core.json.JSON;
import com.lts.jvmmonitor.JVMCollector;

import java.util.Map;

/**
 * 主要用于获取节点的JVM信息
 *
 * @author Robert HG (254963746@qq.com) on 3/10/16.
 */
public class JVMInfoGetHttpCmd implements HttpCmdProc {

    private Config config;

    public JVMInfoGetHttpCmd(Config config) {
        this.config = config;
    }

    @Override
    public String nodeIdentity() {
        return config.getIdentity();
    }

    @Override
    public String getCommand() {
        return HttpCmdNames.HTTP_CMD_JVM_INFO_GET;
    }

    @Override
    public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {

        Map<String, Object> jvmInfo = JVMCollector.getJVMInfo();

        HttpCmdResponse response = new HttpCmdResponse();
        response.setSuccess(true);
        response.setObj(JSON.toJSONString(jvmInfo));

        return response;
    }

}
