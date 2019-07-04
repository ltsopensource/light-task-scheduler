package com.github.ltsopensource.admin.request;

import java.util.Date;
import java.util.Map;
import lombok.Data;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
@Data
public class JobQueueReq {

    // ------------ 下面是查询条件值 ---------------
    private String jobId;
    private String jobType;
    private String taskId;
    private String realTaskId;

    private String submitNodeGroup;

    private String taskTrackerNodeGroup;

    private Date startGmtCreated;
    private Date endGmtCreated;
    private Date startGmtModified;
    private Date endGmtModified;

    // ------------ 下面是能update的值 -------------------

    private String cronExpression;

    private Boolean needFeedback;

    private Map<String, String> extParams;

    private Date triggerTime;

    private Integer priority;

    private Integer maxRetryTimes;

    private Integer repeatCount;

    private Long repeatInterval;

    private Boolean relyOnPrevCycle;

}
