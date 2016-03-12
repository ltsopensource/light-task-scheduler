package com.lts.admin.access.face;

import com.lts.admin.request.MDataPaginationReq;
import com.lts.monitor.access.domain.JobTrackerMDataPo;
import com.lts.monitor.access.face.JobTrackerMAccess;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 9/22/15.
 */
public interface BackendJobTrackerMAccess extends JobTrackerMAccess {

    List<JobTrackerMDataPo> querySum(MDataPaginationReq request);

    void delete(MDataPaginationReq request);

    List<String> getJobTrackers();
}
