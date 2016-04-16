package com.github.ltsopensource.monitor.access.domain;

/**
 * @author Robert HG (254963746@qq.com) on 8/22/15.
 */
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

    public Long getExeSuccessNum() {
        return exeSuccessNum;
    }

    public void setExeSuccessNum(Long exeSuccessNum) {
        this.exeSuccessNum = exeSuccessNum;
    }

    public Long getExeFailedNum() {
        return exeFailedNum;
    }

    public void setExeFailedNum(Long exeFailedNum) {
        this.exeFailedNum = exeFailedNum;
    }

    public Long getExeLaterNum() {
        return exeLaterNum;
    }

    public void setExeLaterNum(Long exeLaterNum) {
        this.exeLaterNum = exeLaterNum;
    }

    public Long getExeExceptionNum() {
        return exeExceptionNum;
    }

    public void setExeExceptionNum(Long exeExceptionNum) {
        this.exeExceptionNum = exeExceptionNum;
    }

    public Long getTotalRunningTime() {
        return totalRunningTime;
    }

    public void setTotalRunningTime(Long totalRunningTime) {
        this.totalRunningTime = totalRunningTime;
    }

}
