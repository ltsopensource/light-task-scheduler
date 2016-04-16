package com.github.ltsopensource.jobclient.domain;


/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 */
public class ResponseCode {

    private ResponseCode(){}

    // 没有找到  JobTracker 节点
    public static final String JOB_TRACKER_NOT_FOUND = "11";

    // 提交失败并且写入文件
    public static final String SUBMIT_FAILED_AND_SAVE_FOR_LATER = "12";

    // 请求参数检查失败
    public static final String REQUEST_FILED_CHECK_ERROR = "13";

    // 提交太块
    public static final String SUBMIT_TOO_BUSY_AND_SAVE_FOR_LATER = "14";

    public static final String SYSTEM_ERROR = "15";
}
