package com.github.ltsopensource.monitor.access.domain;

import com.github.ltsopensource.core.cluster.NodeType;
import lombok.Data;

/**
 * @author Robert HG (254963746@qq.com) on 9/1/15.
 */
@Data
public abstract class MDataPo {

    private String id;
    /**
     * 创建时间
     */
    private Long gmtCreated;
    /**
     * 记录时间(监控数据时间点)
     */
    private Long timestamp;

    private NodeType nodeType;
    /**
     * NodeGroup
     */
    private String nodeGroup;
    /**
     * TaskTracker 节点标识
     */
    private String identity;

}
