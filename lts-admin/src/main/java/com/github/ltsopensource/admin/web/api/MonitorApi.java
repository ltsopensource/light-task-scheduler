package com.github.ltsopensource.admin.web.api;

import com.github.ltsopensource.admin.cluster.BackendAppContext;
import com.github.ltsopensource.admin.request.MDataPaginationReq;
import com.github.ltsopensource.admin.web.AbstractMVC;
import com.github.ltsopensource.admin.web.support.Builder;
import com.github.ltsopensource.admin.web.vo.RestfulResponse;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.monitor.access.domain.MDataPo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/21/15.
 */
@RestController
public class MonitorApi extends AbstractMVC {

    @Autowired
    private BackendAppContext appContext;

    @RequestMapping(value = "/monitor/monitor-data-get", method = {RequestMethod.POST, RequestMethod.GET})
    public RestfulResponse monitorDataGet(MDataPaginationReq request) {
        RestfulResponse response = new RestfulResponse();
        if (request.getNodeType() == null) {
            return Builder.build(false, "nodeType can not be null.");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            return Builder.build(false, "Search time range must be input.");
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

    @RequestMapping(value = "/monitor/jvm-monitor-data-get", method = {RequestMethod.POST, RequestMethod.GET})
    public RestfulResponse jvmMDataGet(MDataPaginationReq request) {
        RestfulResponse response = new RestfulResponse();
        if (request.getJvmType() == null) {
            return Builder.build(false, "jvmType can not be null.");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            return Builder.build(false, "Search time range must be input.");
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
