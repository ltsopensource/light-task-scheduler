package com.github.ltsopensource.admin.request;

import com.github.ltsopensource.core.cluster.NodeType;

import java.util.Date;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
public class NodePaginationReq extends PaginationReq {

    private String identity;
    private String ip;
    private String nodeGroup;
    private NodeType nodeType;
    private Boolean available;
    private Date startDate;
    private Date endDate;

    public NodePaginationReq() {
        // 默认不分页
        setLimit(Integer.MAX_VALUE);
    }

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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
