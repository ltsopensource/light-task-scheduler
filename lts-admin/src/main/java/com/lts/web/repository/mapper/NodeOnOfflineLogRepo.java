package com.lts.web.repository.mapper;

import com.lts.web.repository.domain.NodeOnOfflineLog;
import com.lts.web.request.NodeOnOfflineLogRequest;

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
