package com.github.ltsopensource.core.domain;

/**
 * @author Robert HG (254963746@qq.com) on 6/13/15.
 */
public enum Action {

    EXECUTE_SUCCESS,    // 执行成功,这种情况 直接反馈客户端
    EXECUTE_FAILED,     // 执行失败,这种情况,直接反馈给客户端,不重新执行
    EXECUTE_LATER,       // 稍后重新执行,这种情况, 不反馈客户端,稍后重新执行,不过有最大重试次数
    EXECUTE_EXCEPTION   // 执行异常, 这中情况也会重试

}
