package com.lts.job.client.domain;


/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 */
public class ResponseCode {

    private ResponseCode(){}

    // 没有找到  JobTracker 节点
    public static final String JOB_TRACKER_NOT_FOUND = "JOB_TRACKER_NOT_FOUND";

    // 提交失败并且写入文件
    public static final String FAILED_AND_SAVE_FILE = "FAILED_AND_SAVE_FILE";

    // 请求参数检查失败
    public static final String REQUEST_FILED_CHECK_ERROR = "REQUEST_FILED_CHECK_ERROR";

}
