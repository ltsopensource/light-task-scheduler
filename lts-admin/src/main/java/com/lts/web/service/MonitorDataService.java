package com.lts.web.service;

import com.alibaba.fastjson.TypeReference;
import com.lts.core.commons.utils.BeanUtils;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.JSONUtils;
import com.lts.core.commons.utils.Md5Encrypt;
import com.lts.core.domain.JobTrackerMonitorData;
import com.lts.core.domain.TaskTrackerMonitorData;
import com.lts.core.support.SystemClock;
import com.lts.web.repository.*;
import com.lts.web.request.MonitorDataAddRequest;
import com.lts.web.request.MonitorDataRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 9/1/15.
 */
@Service
public class MonitorDataService {

    @Autowired
    private TaskTrackerMonitorDataRepository taskTrackerMonitorDataRepository;
    @Autowired
    private JobTrackerMonitorDataRepository jobTrackerMonitorDataRepository;

    public void addTaskTrackerMonitorData(MonitorDataAddRequest request) {

        List<TaskTrackerMonitorData> monitorDataList = JSONUtils.parse(request.getMonitorData(), new TypeReference<List<TaskTrackerMonitorData>>() {
        });
        if (CollectionUtils.isEmpty(monitorDataList)) {
            throw new IllegalArgumentException("monitorData can not be null");
        }

        List<TaskTrackerMonitorDataPo> pos = new ArrayList<TaskTrackerMonitorDataPo>(monitorDataList.size());
        for (TaskTrackerMonitorData monitorData : monitorDataList) {
            TaskTrackerMonitorDataPo po = new TaskTrackerMonitorDataPo();

            BeanUtils.copyProperties(po, monitorData);

            po.setTaskTrackerIdentity(request.getIdentity());
            po.setTaskTrackerNodeGroup(request.getNodeGroup());
            po.setId(Md5Encrypt.md5(po.getTaskTrackerIdentity() + monitorData.getTimestamp()));
            po.setGmtCreated(SystemClock.now());

            pos.add(po);
        }
        taskTrackerMonitorDataRepository.insert(pos);
    }

    public List<? extends AbstractMonitorDataPo> queryMonitorDataSum(MonitorDataRequest request){
        switch (request.getNodeType()){
            case JOB_CLIENT:
                return null;
            case JOB_TRACKER:
                return jobTrackerMonitorDataRepository.querySum(request);
            case TASK_TRACKER:
                return taskTrackerMonitorDataRepository.querySum(request);
        }
        return null;
    }

    public void addJobTrackerMonitorData(MonitorDataAddRequest request){
        List<JobTrackerMonitorData> monitorDataList = JSONUtils.parse(request.getMonitorData(), new TypeReference<List<JobTrackerMonitorData>>() {
        });
        if (CollectionUtils.isEmpty(monitorDataList)) {
            throw new IllegalArgumentException("monitorData can not be null");
        }

        List<JobTrackerMonitorDataPo> pos = new ArrayList<JobTrackerMonitorDataPo>(monitorDataList.size());
        for (JobTrackerMonitorData monitorData : monitorDataList) {
            JobTrackerMonitorDataPo po = new JobTrackerMonitorDataPo();

            BeanUtils.copyProperties(po, monitorData);

            po.setJobTrackerIdentity(request.getIdentity());
            po.setId(Md5Encrypt.md5(po.getJobTrackerIdentity() + monitorData.getTimestamp()));
            po.setGmtCreated(SystemClock.now());
            pos.add(po);
        }
        jobTrackerMonitorDataRepository.insert(pos);
    }
    public void addJobClientMonitorData(MonitorDataAddRequest request){

    }
}
