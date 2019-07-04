package com.github.ltsopensource.admin.request;

import com.github.ltsopensource.core.cluster.NodeType;
import java.util.Date;
import lombok.Data;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
@Data
public class NodePaginationReq {

    private String identity;
    private String ip;
    private String nodeGroup;
    private NodeType nodeType;
    private Boolean available;
    private Date startDate;
    private Date endDate;

}
