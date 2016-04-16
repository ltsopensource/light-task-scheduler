package com.github.ltsopensource.admin.access.face;

import com.github.ltsopensource.admin.access.domain.NodeOnOfflineLog;
import com.github.ltsopensource.admin.request.NodeOnOfflineLogPaginationReq;

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
