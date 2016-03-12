package com.lts.admin.access.face;

import com.lts.admin.access.domain.NodeOnOfflineLog;
import com.lts.admin.request.NodeOnOfflineLogPaginationReq;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 9/26/15.
 */
public interface BackendNodeOnOfflineLogAccess {

    void insert(List<NodeOnOfflineLog> nodeOnOfflineLogs);

    List<NodeOnOfflineLog> select(NodeOnOfflineLogPaginationReq request);

    Long count(NodeOnOfflineLogPaginationReq request);

    void delete(NodeOnOfflineLogPaginationReq request);

}
