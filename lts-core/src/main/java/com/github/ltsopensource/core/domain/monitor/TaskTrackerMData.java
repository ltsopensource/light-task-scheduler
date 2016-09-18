package com.github.ltsopensource.core.domain.monitor;

/**
 * TaskTracker Monitor Info(MI)
 *
 * @author Robert HG (254963746@qq.com) on 8/21/15.
 */
public class TaskTrackerMData extends MData {

    private Long exeSuccessNum;
    private Long exeFailedNum;
    private Long exeLaterNum;
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
