package com.lts.job.web.controller;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.web.service.NodeService;
import com.lts.job.web.vo.RestfulResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/11/15.
 */
@RestController
public class NodeController extends AbstractController {

    @Autowired
    NodeService nodeService;

    @RequestMapping("/node/node-list-get/{clusterName}")
    public RestfulResponse getNodeList(@PathVariable String clusterName) {
        RestfulResponse response = new RestfulResponse();

        List<Node> nodes = nodeService.getNodeList(clusterName);
        response.setSuccess(true);
        response.setResults(CollectionUtils.sizeOf(nodes));
        response.setRows(nodes);

        return response;
    }
}
