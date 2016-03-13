package com.lts.admin.web.api;

import com.lts.admin.cluster.BackendAppContext;
import com.lts.admin.request.MDataPaginationReq;
import com.lts.admin.web.AbstractMVC;
import com.lts.admin.web.vo.RestfulResponse;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.StringUtils;
import com.lts.monitor.access.domain.MDataPo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/21/15.
 */
@RestController
public class MonitorApi extends AbstractMVC {

    @Autowired
    private BackendAppContext appContext;

    @RequestMapping("/monitor/monitor-data-get")
    public RestfulResponse monitorDataGet(MDataPaginationReq request) {
        RestfulResponse response = new RestfulResponse();
        if (request.getNodeType() == null) {
            response.setSuccess(false);
            response.setMsg("nodeType can not be null.");
            return response;
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            response.setSuccess(false);
            response.setMsg("Search time range must be input.");
            return response;
        }
        if (StringUtils.isNotEmpty(request.getIdentity())) {
            request.setNodeGroup(null);
        }

        List<? extends MDataPo> rows = null;
        switch (request.getNodeType()) {
            case JOB_CLIENT:
                rows = appContext.getBackendJobClientMAccess().querySum(request);
                break;
            case JOB_TRACKER:
                rows = appContext.getBackendJobTrackerMAccess().querySum(request);
                break;
            case TASK_TRACKER:
                rows = appContext.getBackendTaskTrackerMAccess().querySum(request);
                break;
        }
        response.setSuccess(true);
        response.setRows(rows);
        response.setResults(CollectionUtils.sizeOf(rows));
        return response;
    }

    @RequestMapping("/monitor/jvm-monitor-data-get")
    public RestfulResponse jvmMDataGet(MDataPaginationReq request) {
        RestfulResponse response = new RestfulResponse();
        if (request.getJvmType() == null) {
            response.setSuccess(false);
            response.setMsg("jvmType can not be null.");
            return response;
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            response.setSuccess(false);
            response.setMsg("Search time range must be input.");
            return response;
        }
        if (StringUtils.isNotEmpty(request.getIdentity())) {
            request.setNodeGroup(null);
        }

        List<? extends MDataPo> rows = null;
        switch (request.getJvmType()) {
            case GC:
                rows = appContext.getBackendJVMGCAccess().queryAvg(request);
                break;
            case MEMORY:
                rows = appContext.getBackendJVMMemoryAccess().queryAvg(request);
                break;
            case THREAD:
                rows = appContext.getBackendJVMThreadAccess().queryAvg(request);
                break;
        }
        response.setSuccess(true);
        response.setRows(rows);
        response.setResults(CollectionUtils.sizeOf(rows));
        return response;
    }

}
