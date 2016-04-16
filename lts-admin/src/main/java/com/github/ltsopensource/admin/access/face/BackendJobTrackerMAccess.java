package com.github.ltsopensource.admin.access.face;

import com.github.ltsopensource.admin.request.MDataPaginationReq;
import com.github.ltsopensource.monitor.access.domain.JobTrackerMDataPo;
import com.github.ltsopensource.monitor.access.face.JobTrackerMAccess;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 9/22/15.
 */
public interface BackendJobTrackerMAccess extends JobTrackerMAccess {

    List<JobTrackerMDataPo> querySum(MDataPaginationReq request);

    void delete(MDataPaginationReq request);

    List<String> getJobTrackers();
}
