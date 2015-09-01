package com.lts.web.request;

import com.lts.core.cluster.NodeType;

/**
 * @author Robert HG (254963746@qq.com) on 8/21/15.
 */
public class MonitorDataAddRequest {

    private NodeType nodeType;
    /**
     * NodeGroup
     */
    private String nodeGroup;
    /**
     * TaskTracker 节点标识
     */
    private String identity;
    /**
     * JSON List<MonitorData>
     */
    private String monitorData;

    public String getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getMonitorData() {
        return monitorData;
    }

    public void setMonitorData(String monitorData) {
        this.monitorData = monitorData;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }
}
