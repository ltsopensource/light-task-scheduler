package com.lts.admin.access.mapper;

import com.lts.admin.access.domain.JobTrackerMonitorDataPo;
import com.lts.admin.request.MonitorDataRequest;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 9/22/15.
 */
public interface JobTrackerMonitorRepo {

    void insert(List<JobTrackerMonitorDataPo> pos);

    List<JobTrackerMonitorDataPo> querySum(MonitorDataRequest request);

    void delete(MonitorDataRequest request);

    List<String> getJobTrackers();
}
