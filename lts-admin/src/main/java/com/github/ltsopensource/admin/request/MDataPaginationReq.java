package com.github.ltsopensource.admin.request;

import com.github.ltsopensource.core.cluster.NodeType;
import lombok.Data;

/**
 * @author Robert HG (254963746@qq.com) on 8/22/15.
 */
@Data
public class MDataPaginationReq {

    private NodeType nodeType;

    private String id;

    private String nodeGroup;

    private String identity;

    private Long startTime;

    private Long endTime;


}
