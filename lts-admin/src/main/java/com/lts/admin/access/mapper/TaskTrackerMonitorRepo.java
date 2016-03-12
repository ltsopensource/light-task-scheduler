package com.lts.admin.access.mapper;

import com.lts.admin.access.domain.TaskTrackerMonitorDataPo;
import com.lts.admin.request.MonitorDataRequest;

import java.util.List;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 9/22/15.
 */
public interface TaskTrackerMonitorRepo {

    void insert(List<TaskTrackerMonitorDataPo> pos);

    List<TaskTrackerMonitorDataPo> querySum(MonitorDataRequest request);

    void delete(MonitorDataRequest request);

    List<Map<String, String>> getTaskTrackerMap();
}
