package com.github.ltsopensource.admin.web.api;

import com.github.ltsopensource.admin.access.domain.NodeOnOfflineLog;
import com.github.ltsopensource.admin.cluster.BackendAppContext;
import com.github.ltsopensource.admin.request.NodeGroupRequest;
import com.github.ltsopensource.admin.request.NodeOnOfflineLogPaginationReq;
import com.github.ltsopensource.admin.request.NodePaginationReq;
import com.github.ltsopensource.admin.response.PaginationRsp;
import com.github.ltsopensource.admin.web.AbstractMVC;
import com.github.ltsopensource.admin.web.vo.RestfulResponse;
import com.github.ltsopensource.core.cluster.Node;
import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.domain.NodeGroupGetReq;
import com.github.ltsopensource.queue.domain.NodeGroupPo;
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
    private BackendAppContext appContext;

    @RequestMapping("node-list-get")
    public RestfulResponse getNodeList(NodePaginationReq request) {
        RestfulResponse response = new RestfulResponse();
        request.setLimit(10000);
        PaginationRsp<Node> paginationRsp = appContext.getBackendRegistrySrv().getOnlineNodes(request);

        response.setSuccess(true);
        response.setResults(paginationRsp.getResults());
        response.setRows(paginationRsp.getRows());

        return response;
    }

    @RequestMapping("registry-re-subscribe")
    public RestfulResponse reSubscribe() {
        RestfulResponse response = new RestfulResponse();

        appContext.getBackendRegistrySrv().reSubscribe();

        response.setSuccess(true);
        return response;
    }

    @RequestMapping("node-group-get")
    public RestfulResponse getNodeGroup(NodeGroupGetReq nodeGroupGetReq) {
        RestfulResponse response = new RestfulResponse();
        PaginationRsp<NodeGroupPo> paginationRsp = appContext.getNodeGroupStore().getNodeGroup(nodeGroupGetReq);

        response.setResults(paginationRsp.getResults());
        response.setRows(paginationRsp.getRows());
        response.setSuccess(true);
        return response;
    }

    @RequestMapping("node-group-add")
    public RestfulResponse addNodeGroup(NodeGroupRequest request) {
        RestfulResponse response = new RestfulResponse();
        appContext.getNodeGroupStore().addNodeGroup(request.getNodeType(), request.getNodeGroup());
        if (NodeType.TASK_TRACKER.equals(request.getNodeType())) {
            appContext.getExecutableJobQueue().createQueue(request.getNodeGroup());
        } else if (NodeType.JOB_CLIENT.equals(request.getNodeType())) {
            appContext.getJobFeedbackQueue().createQueue(request.getNodeGroup());
        }
        response.setSuccess(true);
        return response;
    }

    @RequestMapping("node-group-del")
    public RestfulResponse delNodeGroup(NodeGroupRequest request) {
        RestfulResponse response = new RestfulResponse();
        appContext.getNodeGroupStore().removeNodeGroup(request.getNodeType(), request.getNodeGroup());
        if (NodeType.TASK_TRACKER.equals(request.getNodeType())) {
            appContext.getExecutableJobQueue().removeQueue(request.getNodeGroup());
        } else if (NodeType.JOB_CLIENT.equals(request.getNodeType())) {
            appContext.getJobFeedbackQueue().removeQueue(request.getNodeGroup());
        }
        response.setSuccess(true);
        return response;
    }

    @RequestMapping("node-onoffline-log-get")
    public RestfulResponse getNodeOnofflineLog(NodeOnOfflineLogPaginationReq request) {
        RestfulResponse response = new RestfulResponse();
        Long results = appContext.getBackendNodeOnOfflineLogAccess().count(request);
        response.setResults(results.intValue());
        if (results > 0) {
            List<NodeOnOfflineLog> rows = appContext.getBackendNodeOnOfflineLogAccess().select(request);
            response.setRows(rows);
        } else {
            response.setRows(new ArrayList<Object>(0));
        }
        response.setSuccess(true);
        return response;
    }
}
