package com.lts.job.queue.domain;

import com.lts.job.core.cluster.NodeType;

/**
 * Created by hugui on 6/7/15.
 */
public class NodeGroupPo {

    private NodeType nodeType;
    /**
     * 名称
     */
    private String name;
    /**
     * 创建时间
     */
    private Long gmtCreated;

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getGmtCreated() {
        return gmtCreated;
    }

    public void setGmtCreated(Long gmtCreated) {
        this.gmtCreated = gmtCreated;
    }
}
