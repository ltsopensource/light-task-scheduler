package com.github.ltsopensource.monitor.access.domain;

/**
 * @author Robert HG (254963746@qq.com) on 3/12/16.
 */
public class JobClientMDataPo extends MDataPo {

    // 提交成功的个数
    private Long submitSuccessNum;
    // 提交失败的个数
    private Long submitFailedNum;
    // 存储FailStore的个数
    private Long failStoreNum;
    // 提交FailStore的个数
    private Long submitFailStoreNum;
    // 处理的反馈的个数
    private Long handleFeedbackNum;

    public Long getSubmitSuccessNum() {
        return submitSuccessNum;
    }

    public void setSubmitSuccessNum(Long submitSuccessNum) {
        this.submitSuccessNum = submitSuccessNum;
    }

    public Long getSubmitFailedNum() {
        return submitFailedNum;
    }

    public void setSubmitFailedNum(Long submitFailedNum) {
        this.submitFailedNum = submitFailedNum;
    }

    public Long getFailStoreNum() {
        return failStoreNum;
    }

    public void setFailStoreNum(Long failStoreNum) {
        this.failStoreNum = failStoreNum;
    }

    public Long getSubmitFailStoreNum() {
        return submitFailStoreNum;
    }

    public void setSubmitFailStoreNum(Long submitFailStoreNum) {
        this.submitFailStoreNum = submitFailStoreNum;
    }

    public Long getHandleFeedbackNum() {
        return handleFeedbackNum;
    }

    public void setHandleFeedbackNum(Long handleFeedbackNum) {
        this.handleFeedbackNum = handleFeedbackNum;
    }
}
