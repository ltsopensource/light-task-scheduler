package com.lts.core.support;

/**
 * @author Robert HG (254963746@qq.com) on 5/28/15.
 */
public class JobQueueUtils {

    private JobQueueUtils() {
    }

    /**
     * 在数据库中就是表名, taskTrackerNodeGroup 是 TaskTracker的 nodeGroup
     */
    public static String getExecutableQueueName(String taskTrackerNodeGroup) {
        return "lts_executable_job_queue_".concat(taskTrackerNodeGroup);
    }

    /**
     * 在数据库中就是表名, jobClientNodeGroup 是 JobClient 的 nodeGroup
     */
    public static String getFeedbackQueueName(String jobClientNodeGroup) {
        return "lts_feedback_job_queue_".concat(jobClientNodeGroup);
    }

    public static final String CRON_JOB_QUEUE = "lts_cron_job_queue";

    public static final String EXECUTING_JOB_QUEUE = "lts_executing_job_queue";

    public static final String NODE_GROUP_STORE = "lts_node_group_store";
}
