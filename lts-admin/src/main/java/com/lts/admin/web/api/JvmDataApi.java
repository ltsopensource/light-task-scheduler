package com.lts.admin.web.api;

import com.lts.admin.cluster.BackendAppContext;
import com.lts.admin.request.NodePaginationReq;
import com.lts.admin.support.I18nManager;
import com.lts.admin.web.AbstractMVC;
import com.lts.admin.web.vo.RestfulResponse;
import com.lts.cmd.DefaultHttpCmd;
import com.lts.cmd.HttpCmd;
import com.lts.cmd.HttpCmdClient;
import com.lts.cmd.HttpCmdResponse;
import com.lts.core.cluster.Node;
import com.lts.core.cmd.HttpCmdNames;
import com.lts.core.commons.utils.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

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

        NodePaginationReq nodeRequest = new NodePaginationReq();
        nodeRequest.setIdentity(identity);
        List<Node> nodes = appContext.getNodeMemCacheAccess().search(nodeRequest);

        if (CollectionUtils.isEmpty(nodes)) {
            restfulResponse.setSuccess(false);
            restfulResponse.setMsg(I18nManager.getMessage("node.dose.not.alive"));
            return restfulResponse;
        }

        Node node = nodes.get(0);

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
