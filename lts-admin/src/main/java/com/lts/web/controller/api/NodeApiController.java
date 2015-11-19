package com.lts.web.controller.api;

import com.lts.core.cluster.Node;
import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.domain.NodeGroupGetRequest;
import com.lts.queue.domain.NodeGroupPo;
import com.lts.web.cluster.AdminApplication;
import com.lts.web.cluster.RegistryService;
import com.lts.web.controller.AbstractController;
import com.lts.web.repository.domain.NodeOnOfflineLog;
import com.lts.web.repository.mapper.NodeOnOfflineLogRepo;
import com.lts.web.request.NodeGroupRequest;
import com.lts.web.request.NodeOnOfflineLogRequest;
import com.lts.web.request.NodeRequest;
import com.lts.web.response.PageResponse;
import com.lts.web.vo.RestfulResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/11/15.
 */
@RestController
@RequestMapping("/node")
public class NodeApiController extends AbstractController {

    @Autowired
    RegistryService registryService;
    @Autowired
    AdminApplication application;
    @Autowired
    NodeOnOfflineLogRepo nodeOnOfflineLogRepo;

    @RequestMapping("node-list-get")
    public RestfulResponse getNodeList(NodeRequest request) {
        RestfulResponse response = new RestfulResponse();

        List<Node> nodes = registryService.getOnlineNodes(request);

        response.setSuccess(true);
        response.setResults(CollectionUtils.sizeOf(nodes));
        response.setRows(nodes);

        return response;
    }

    @RequestMapping("node-group-get")
    public RestfulResponse getNodeGroup(NodeGroupRequest request){
        RestfulResponse response = new RestfulResponse();
        NodeGroupGetRequest nodeGroupGetRequest = new NodeGroupGetRequest();
        nodeGroupGetRequest.setNodeGroup(request.getNodeGroup());
        nodeGroupGetRequest.setNodeType(request.getNodeType());
        PageResponse<NodeGroupPo> pageResponse = application.getNodeGroupStore().getNodeGroup(nodeGroupGetRequest);

        response.setResults(pageResponse.getResults());
        response.setRows(pageResponse.getRows());
        response.setSuccess(true);
        return response;
    }

    @RequestMapping("node-group-add")
    public RestfulResponse addNodeGroup(NodeGroupRequest request) {
        RestfulResponse response = new RestfulResponse();
        application.getNodeGroupStore().addNodeGroup(request.getNodeType(), request.getNodeGroup());
        if(NodeType.TASK_TRACKER.equals(request.getNodeType())){
            application.getExecutableJobQueue().createQueue(request.getNodeGroup());
        }else if(NodeType.JOB_CLIENT.equals(request.getNodeType())){
            application.getJobFeedbackQueue().createQueue(request.getNodeGroup());
        }
        response.setSuccess(true);
        return response;
    }

    @RequestMapping("node-group-del")
    public RestfulResponse delNodeGroup(NodeGroupRequest request) {
        RestfulResponse response = new RestfulResponse();
        application.getNodeGroupStore().removeNodeGroup(request.getNodeType(), request.getNodeGroup());
        if(NodeType.TASK_TRACKER.equals(request.getNodeType())){
            application.getExecutableJobQueue().removeQueue(request.getNodeGroup());
        }else if(NodeType.JOB_CLIENT.equals(request.getNodeType())){
            application.getJobFeedbackQueue().removeQueue(request.getNodeGroup());
        }
        response.setSuccess(true);
        return response;
    }

    @RequestMapping("node-onoffline-log-get")
    public RestfulResponse delNodeGroup(NodeOnOfflineLogRequest request) {
        RestfulResponse response = new RestfulResponse();
        Long results = nodeOnOfflineLogRepo.count(request);
        response.setResults(results.intValue());
        if(results > 0){
            List<NodeOnOfflineLog> rows = nodeOnOfflineLogRepo.select(request);
            response.setRows(rows);
        }else{
            response.setRows(new ArrayList<Object>(0));
        }
        response.setSuccess(true);
        return response;
    }
}
