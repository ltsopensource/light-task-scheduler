package com.lts.admin.web.api;

import com.lts.admin.access.domain.NodeOnOfflineLog;
import com.lts.admin.cluster.BackendAppContext;
import com.lts.admin.cluster.BackendRegistryService;
import com.lts.admin.request.NodeGroupRequest;
import com.lts.admin.request.NodeOnOfflineLogPaginationReq;
import com.lts.admin.request.NodePaginationReq;
import com.lts.admin.response.PaginationRsp;
import com.lts.admin.web.AbstractMVC;
import com.lts.admin.web.vo.RestfulResponse;
import com.lts.core.cluster.Node;
import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.domain.NodeGroupGetRequest;
import com.lts.queue.domain.NodeGroupPo;
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
public class NodeApi extends AbstractMVC {

    @Autowired
    BackendRegistryService backendRegistryService;
    @Autowired
    BackendAppContext appContext;

    @RequestMapping("node-list-get")
    public RestfulResponse getNodeList(NodePaginationReq request) {
        RestfulResponse response = new RestfulResponse();

        List<Node> nodes = backendRegistryService.getOnlineNodes(request);

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
        PaginationRsp<NodeGroupPo> paginationRsp = appContext.getNodeGroupStore().getNodeGroup(nodeGroupGetRequest);

        response.setResults(paginationRsp.getResults());
        response.setRows(paginationRsp.getRows());
        response.setSuccess(true);
        return response;
    }

    @RequestMapping("node-group-add")
    public RestfulResponse addNodeGroup(NodeGroupRequest request) {
        RestfulResponse response = new RestfulResponse();
        appContext.getNodeGroupStore().addNodeGroup(request.getNodeType(), request.getNodeGroup());
        if(NodeType.TASK_TRACKER.equals(request.getNodeType())){
            appContext.getExecutableJobQueue().createQueue(request.getNodeGroup());
        }else if(NodeType.JOB_CLIENT.equals(request.getNodeType())){
            appContext.getJobFeedbackQueue().createQueue(request.getNodeGroup());
        }
        response.setSuccess(true);
        return response;
    }

    @RequestMapping("node-group-del")
    public RestfulResponse delNodeGroup(NodeGroupRequest request) {
        RestfulResponse response = new RestfulResponse();
        appContext.getNodeGroupStore().removeNodeGroup(request.getNodeType(), request.getNodeGroup());
        if(NodeType.TASK_TRACKER.equals(request.getNodeType())){
            appContext.getExecutableJobQueue().removeQueue(request.getNodeGroup());
        }else if(NodeType.JOB_CLIENT.equals(request.getNodeType())){
            appContext.getJobFeedbackQueue().removeQueue(request.getNodeGroup());
        }
        response.setSuccess(true);
        return response;
    }

    @RequestMapping("node-onoffline-log-get")
    public RestfulResponse delNodeGroup(NodeOnOfflineLogPaginationReq request) {
        RestfulResponse response = new RestfulResponse();
        Long results = appContext.getBackendNodeOnOfflineLogAccess().count(request);
        response.setResults(results.intValue());
        if(results > 0){
            List<NodeOnOfflineLog> rows = appContext.getBackendNodeOnOfflineLogAccess().select(request);
            response.setRows(rows);
        }else{
            response.setRows(new ArrayList<Object>(0));
        }
        response.setSuccess(true);
        return response;
    }
}
