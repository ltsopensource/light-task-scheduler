package com.github.ltsopensource.monitor.access.domain;

import lombok.Data;

/**
 * @author Robert HG (254963746@qq.com) on 8/22/15.
 */
@Data
public class TaskTrackerMDataPo extends MDataPo {

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
     * 总的运行时间
     */
    private Long totalRunningTime;

}
