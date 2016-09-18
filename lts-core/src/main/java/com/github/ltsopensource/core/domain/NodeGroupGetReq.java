package com.github.ltsopensource.core.domain;

import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.admin.request.PaginationReq;

/**
 * @author Robert HG (254963746@qq.com) on 9/5/15.
 */
public class NodeGroupGetReq extends PaginationReq {

    private NodeType nodeType;

    private String nodeGroup;

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }
}
