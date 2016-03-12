package com.lts.admin.access.face;

import com.lts.admin.request.MDataPaginationReq;
import com.lts.admin.web.vo.NodeInfo;
import com.lts.core.domain.monitor.JobClientMData;
import com.lts.monitor.access.domain.JobClientMDataPo;
import com.lts.monitor.access.face.JobClientMAccess;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/12/16.
 */
public interface BackendJobClientMAccess extends JobClientMAccess {

    void delete(MDataPaginationReq request);

    List<JobClientMDataPo> querySum(MDataPaginationReq request);

    List<NodeInfo> getJobClients();
}
