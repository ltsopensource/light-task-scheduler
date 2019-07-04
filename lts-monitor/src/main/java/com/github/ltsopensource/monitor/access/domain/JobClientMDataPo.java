package com.github.ltsopensource.monitor.access.domain;

import lombok.Data;

/**
 * @author Robert HG (254963746@qq.com) on 3/12/16.
 */
@Data
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
}
