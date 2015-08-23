package com.lts.web.request;

/**
 * @author Robert HG (254963746@qq.com) on 8/21/15.
 */
public class TaskTrackerMIAddRequest {

    /**
     * TaskTracker NodeGroup
     */
    private String nodeGroup;
    /**
     * TaskTracker 节点标识
     */
    private String identity;
    /**
     * JSON List<TaskTrackerMI>
     */
    private String mis;

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

    public String getMis() {
        return mis;
    }

    public void setMis(String mis) {
        this.mis = mis;
    }
}
