package com.lts.web.repository.mapper;

import com.lts.web.repository.domain.JobTrackerMonitorDataPo;
import com.lts.web.repository.domain.TaskTrackerMonitorDataPo;
import com.lts.web.request.MonitorDataRequest;

import java.util.List;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 9/22/15.
 */
public interface JobTrackerMonitorRepository extends AbstractRepository {

    void insert(List<JobTrackerMonitorDataPo> pos);

    List<JobTrackerMonitorDataPo> querySum(MonitorDataRequest request);

    void delete(MonitorDataRequest request);

    List<String> getJobTrackers();
}
