package com.lts.web.controller.api;

import com.alibaba.fastjson.TypeReference;
import com.lts.core.commons.utils.Assert;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.JSONUtils;
import com.lts.core.commons.utils.Md5Encrypt;
import com.lts.core.domain.TaskTrackerMI;
import com.lts.core.support.SystemClock;
import com.lts.web.controller.AbstractController;
import com.lts.web.repository.TaskTrackerMIPo;
import com.lts.web.repository.TaskTrackerMIRepository;
import com.lts.web.request.TaskTrackerMIAddRequest;
import com.lts.web.request.TaskTrackerMIRequest;
import com.lts.web.vo.RestfulResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/21/15.
 */
@RestController
public class MonitorApiController extends AbstractController {

    @Autowired
    TaskTrackerMIRepository monitorRepository;

    @RequestMapping("/monitor/tasktracker-mi-add")
    public RestfulResponse taskTrackerMonitorInfoAdd(TaskTrackerMIAddRequest request) {
        RestfulResponse response = new RestfulResponse();

        try {
            Assert.hasText(request.getIdentity(), "identity can not be null");
            Assert.hasText(request.getNodeGroup(), "nodeGroup can not be null");
            Assert.hasText(request.getMis(), "mis can not be null");

            List<TaskTrackerMI> mis = JSONUtils.parse(request.getMis(), new TypeReference<List<TaskTrackerMI>>() {
            });
            if (CollectionUtils.isEmpty(mis)) {
                response.setSuccess(false);
                response.setMsg("mis can not be null");
                return response;
            }

            List<TaskTrackerMIPo> pos = new ArrayList<TaskTrackerMIPo>(mis.size());
            for (TaskTrackerMI mi : mis) {
                TaskTrackerMIPo po = new TaskTrackerMIPo();
                po.setTaskTrackerIdentity(request.getIdentity());
                po.setTaskTrackerNodeGroup(request.getNodeGroup());
                po.setTimestamp(mi.getTimestamp());
                po.setId(Md5Encrypt.md5(po.getTaskTrackerIdentity() + mi.getTimestamp()));
                po.setTotalRunningTime(mi.getTotalRunningTime());
                po.setSuccessNum(mi.getSuccessNum());
                po.setFailedNum(mi.getFailedNum());
                po.setFailStoreSize(mi.getFailStoreSize());
                po.setGmtCreated(SystemClock.now());
                po.setMaxMemory(mi.getMaxMemory());
                po.setFreeMemory(mi.getFreeMemory());
                po.setAllocatedMemory(mi.getAllocatedMemory());
                po.setTotalFreeMemory(mi.getTotalFreeMemory());
                pos.add(po);
            }
            monitorRepository.insert(pos);
            response.setSuccess(true);
            return response;

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMsg(e.getMessage());
            return response;
        }
    }

    @RequestMapping("/monitor/tasktracker-mi-get")
    public RestfulResponse taskTrackerMonitorInfoGet(TaskTrackerMIRequest request) {
        RestfulResponse response = new RestfulResponse();
        if (request.getStartTime() == null || request.getEndTime() == null) {
            response.setSuccess(false);
            response.setMsg("Search time range must be input.");
            return response;
        }

        List<TaskTrackerMIPo> taskTrackerMIPos = monitorRepository.querySum(request);
        response.setSuccess(true);
        response.setRows(taskTrackerMIPos);
        response.setResults(CollectionUtils.sizeOf(taskTrackerMIPos));
        return response;
    }

}
