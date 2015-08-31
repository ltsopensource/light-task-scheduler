package com.lts.web.controller.api;

import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.Assert;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.web.controller.AbstractController;
import com.lts.web.repository.AbstractMonitorDataPo;
import com.lts.web.request.MonitorDataAddRequest;
import com.lts.web.request.MonitorDataRequest;
import com.lts.web.service.MonitorDataService;
import com.lts.web.vo.RestfulResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/21/15.
 */
@RestController
public class MonitorApiController extends AbstractController {

    @Autowired
    private MonitorDataService monitorDataService;

    @RequestMapping("/monitor/monitor-data-add")
    public RestfulResponse taskTrackerMonitorInfoAdd(MonitorDataAddRequest request) {
        RestfulResponse response = new RestfulResponse();

        try {
            Assert.notNull(request.getNodeType(), "nodeType can not be null");
            Assert.hasText(request.getIdentity(), "identity can not be null");
            Assert.hasText(request.getNodeGroup(), "nodeGroup can not be null");
            Assert.hasText(request.getMonitorData(), "monitorData can not be null");

            if (NodeType.TASK_TRACKER.equals(request.getNodeType())) {
                monitorDataService.addTaskTrackerMonitorData(request);
            } else if (NodeType.JOB_TRACKER.equals(request.getNodeType())) {
                monitorDataService.addJobTrackerMonitorData(request);
            }else if(NodeType.JOB_CLIENT.equals(request.getNodeType())){
                monitorDataService.addJobClientMonitorData(request);
            }
            response.setSuccess(true);
            return response;

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMsg(e.getMessage());
            return response;
        }
    }

    @RequestMapping("/monitor/monitor-data-get")
    public RestfulResponse taskTrackerMonitorInfoGet(MonitorDataRequest request) {
        RestfulResponse response = new RestfulResponse();
        if(request.getNodeType() == null){
            response.setSuccess(false);
            response.setMsg("nodeTyope can not be null.");
            return response;
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            response.setSuccess(false);
            response.setMsg("Search time range must be input.");
            return response;
        }
        List<? extends AbstractMonitorDataPo> rows = monitorDataService.queryMonitorDataSum(request);
        response.setSuccess(true);
        response.setRows(rows);
        response.setResults(CollectionUtils.sizeOf(rows));
        return response;
    }

}
