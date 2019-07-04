package com.github.ltsopensource.admin.request;

import java.util.Date;
import lombok.Data;

/**
 * @author Robert HG (254963746@qq.com) on 9/26/15.
 */
@Data
public class NodeOnOfflineLogPaginationReq {

    private Date startLogTime;

    private Date endLogTime;

    private String group;

    private String identity;

    private String event;


}
