package com.lts.job.core.cluster;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 6/22/14.
 * 节点
 */
public class Node {

    // 节点路径(zookeeper 上的路径)
    private String path;
    // 是否可用
    private boolean isAvailable = true;

    private NodeType nodeType;
    private String ip;
    private Integer port;
    private String group;
    private Long createTime = System.currentTimeMillis();
    // 线程个数
    private Integer threads;
    // 唯一标识
    private String identity;

    // 自己关注的节点类型
    private List<NodeType> listenNodeTypes;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public List<NodeType> getListenNodeTypes() {
        return listenNodeTypes;
    }

    public void setListenNodeTypes(List<NodeType> listenNodeTypes) {
        this.listenNodeTypes = listenNodeTypes;
    }

    public void addListenNodeType(NodeType nodeType) {
        if (this.listenNodeTypes == null) {
            this.listenNodeTypes = new ArrayList<NodeType>();
        }
        this.listenNodeTypes.add(nodeType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;

        Node node = (Node) o;

        if (!identity.equals(node.identity)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }

    public String getAddress() {
        return ip + ":" + port;
    }

    @Override
    public String toString() {
        return "Node{" +
                "path='" + path + '\'' +
                ", isAvailable=" + isAvailable +
                ", nodeType=" + nodeType +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", group='" + group + '\'' +
                ", createTime=" + createTime +
                ", threads=" + threads +
                ", identity='" + identity + '\'' +
                ", listenNodeTypes=" + listenNodeTypes +
                '}';
    }
}
