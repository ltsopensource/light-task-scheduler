package com.github.ltsopensource.queue;

import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.core.domain.NodeGroupGetReq;
import com.github.ltsopensource.queue.domain.NodeGroupPo;
import com.github.ltsopensource.admin.response.PaginationRsp;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 6/7/15.
 */
public interface NodeGroupStore {

    /**
     * 添加 NodeGroup
     */
    void addNodeGroup(NodeType nodeType, String name);

    /**
     * 移除 NodeGroup
     */
    void removeNodeGroup(NodeType nodeType, String name);

    /**
     * 得到某个nodeType 的所有 nodeGroup
     */
    List<NodeGroupPo> getNodeGroup(NodeType nodeType);

    /**
     * 分页查询
     */
    PaginationRsp<NodeGroupPo> getNodeGroup(NodeGroupGetReq request);
}
