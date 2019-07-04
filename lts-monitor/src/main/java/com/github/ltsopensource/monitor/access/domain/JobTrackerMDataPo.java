package com.github.ltsopensource.monitor.access.domain;

import lombok.Data;

/**
 * @author Robert HG (254963746@qq.com) on 9/1/15.
 */
@Data
public class JobTrackerMDataPo extends MDataPo {

    /**
     * 接受的任务数
     */
    private Long receiveJobNum;
    /**
     * 分发出去的任务数
     */
    private Long pushJobNum;
    /**
     * 执行成功个数
     */
    private Long exeSuccessNum;
    /**
     * 执行失败个数
     */
    private Long exeFailedNum;
    /**
     * 延迟执行个数
     */
    private Long exeLaterNum;
    /**
     * 执行异常个数
     */
    private Long exeExceptionNum;
    /**
     * 修复死任务数
     */
    private Long fixExecutingJobNum;


}
