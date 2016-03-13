package com.lts.admin.web.api;

import com.lts.admin.cluster.BackendAppContext;
import com.lts.admin.support.I18nManager;
import com.lts.admin.web.AbstractMVC;
import com.lts.admin.web.vo.RestfulResponse;
import com.lts.cmd.DefaultHttpCmd;
import com.lts.cmd.HttpCmd;
import com.lts.cmd.HttpCmdClient;
import com.lts.cmd.HttpCmdResponse;
import com.lts.core.cluster.Node;
import com.lts.core.cmd.HttpCmdNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

/**
 * @author Robert HG (254963746@qq.com) on 9/28/15.
 */
@RestController
@RequestMapping("/jvm")
public class JvmDataApi extends AbstractMVC {

    @Autowired
    private BackendAppContext appContext;

    @RequestMapping("node-jvm-info-get")
    public RestfulResponse getNodeList(String identity) {

        RestfulResponse restfulResponse = new RestfulResponse();

        Node node = appContext.getNodeMemCacheAccess().getNodeByIdentity(identity);

        if (node == null) {
            restfulResponse.setSuccess(false);
            restfulResponse.setMsg(I18nManager.getMessage("node.dose.not.alive"));
            return restfulResponse;
        }

        HttpCmd cmd = new DefaultHttpCmd();
        cmd.setCommand(HttpCmdNames.HTTP_CMD_JVM_INFO_GET);
        cmd.setNodeIdentity(identity);

        HttpCmdResponse response = HttpCmdClient.doGet(node.getIp(), node.getHttpCmdPort(), cmd);
        if (response.isSuccess()) {
            restfulResponse.setSuccess(true);
            restfulResponse.setResults(1);
            restfulResponse.setRows(Collections.singletonList(response.getObj()));
        } else {
            restfulResponse.setSuccess(false);
            restfulResponse.setMsg(response.getMsg());
        }

        return restfulResponse;
    }

}
