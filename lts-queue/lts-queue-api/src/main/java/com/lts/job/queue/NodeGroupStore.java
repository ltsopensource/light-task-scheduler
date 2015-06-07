package com.lts.job.queue;

import com.lts.job.core.cluster.NodeType;
import com.lts.job.queue.domain.NodeGroupPo;

import java.util.List;

/**
 * Created by hugui on 6/7/15.
 */
public interface NodeGroupStore {

    /**
     * 添加 NodeGroup
     *
     * @param nodeType
     * @param name
     */
    void addNodeGroup(NodeType nodeType, String name);

    /**
     * 移除 NodeGroup
     *
     * @param nodeType
     * @param name
     */
    void removeNodeGroup(NodeType nodeType, String name);

    /**
     * 得到某个nodeType 的所有 nodeGroup
     *
     * @param nodeType
     * @return
     */
    List<NodeGroupPo> getNodeGroup(NodeType nodeType);

}
