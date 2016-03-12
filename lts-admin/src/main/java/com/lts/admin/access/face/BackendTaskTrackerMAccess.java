package com.lts.admin.access.face;

import com.lts.admin.request.MDataPaginationReq;
import com.lts.admin.web.vo.NodeInfo;
import com.lts.monitor.access.domain.TaskTrackerMDataPo;
import com.lts.monitor.access.face.TaskTrackerMAccess;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 9/22/15.
 */
public interface BackendTaskTrackerMAccess extends TaskTrackerMAccess{

    List<TaskTrackerMDataPo> querySum(MDataPaginationReq request);

    void delete(MDataPaginationReq request);

    List<NodeInfo> getTaskTrackers();
}
