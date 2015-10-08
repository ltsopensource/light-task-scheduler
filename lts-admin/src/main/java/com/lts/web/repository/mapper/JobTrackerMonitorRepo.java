package com.lts.web.repository.mapper;

import com.lts.web.repository.domain.JobTrackerMonitorDataPo;
import com.lts.web.request.MonitorDataRequest;

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
