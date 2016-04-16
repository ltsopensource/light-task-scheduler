package com.github.ltsopensource.core.domain.monitor;

/**
 * @author Robert HG (254963746@qq.com) on 8/31/15.
 */
public class JobTrackerMData extends MData {

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

    public Long getReceiveJobNum() {
        return receiveJobNum;
    }

    public void setReceiveJobNum(Long receiveJobNum) {
        this.receiveJobNum = receiveJobNum;
    }

    public Long getPushJobNum() {
        return pushJobNum;
    }

    public void setPushJobNum(Long pushJobNum) {
        this.pushJobNum = pushJobNum;
    }

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

    public Long getFixExecutingJobNum() {
        return fixExecutingJobNum;
    }

    public void setFixExecutingJobNum(Long fixExecutingJobNum) {
        this.fixExecutingJobNum = fixExecutingJobNum;
    }
}
