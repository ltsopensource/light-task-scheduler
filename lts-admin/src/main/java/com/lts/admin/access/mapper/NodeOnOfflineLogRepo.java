package com.lts.admin.access.mapper;

import com.lts.admin.access.domain.NodeOnOfflineLog;
import com.lts.admin.request.NodeOnOfflineLogRequest;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 9/26/15.
 */
public interface NodeOnOfflineLogRepo {

    void insert(List<NodeOnOfflineLog> nodeOnOfflineLogs);

    List<NodeOnOfflineLog> select(NodeOnOfflineLogRequest request);

    Long count(NodeOnOfflineLogRequest request);

    void delete(NodeOnOfflineLogRequest request);

}
