package com.lts.job.web.controller.api;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.commons.utils.CollectionUtils;
import com.lts.job.core.commons.utils.StringUtils;
import com.lts.job.web.cluster.RegistryService;
import com.lts.job.web.controller.AbstractController;
import com.lts.job.web.request.NodeRequest;
import com.lts.job.web.vo.RestfulResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/11/15.
 */
@RestController
@RequestMapping("/node")
public class NodeApiController extends AbstractController {

    @Autowired
    RegistryService registryService;

    @RequestMapping("node-list-get")
    public RestfulResponse getNodeList(NodeRequest request) {
        RestfulResponse response = new RestfulResponse();

        List<Node> nodes = registryService.getNodes(request);

        response.setSuccess(true);
        response.setResults(CollectionUtils.sizeOf(nodes));
        response.setRows(nodes);

        return response;
    }

    @RequestMapping("node-cluster-register")
    public RestfulResponse registerClusterNameSub(NodeRequest request) {
        RestfulResponse response = new RestfulResponse();

        if (StringUtils.isEmpty(request.getClusterName())) {
            response.setSuccess(false);
            response.setMsg("clusterName can not be null");
            return response;
        }
        registryService.register(request.getClusterName());
        response.setSuccess(true);
        return response;
    }

    @RequestMapping("node-cluster-register-get")
    public RestfulResponse getAllRegisterClusterName() {
        RestfulResponse response = new RestfulResponse();
        List<String> clusterNames = registryService.getAllClusterNames();
        response.setSuccess(true);
        response.setResults(CollectionUtils.sizeOf(clusterNames));
        response.setRows(clusterNames);
        return response;
    }

}
