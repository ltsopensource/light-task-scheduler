package com.lts.alarm;

/**
 * @author Robert HG (254963746@qq.com)  on 2/17/16.
 */
public interface AlarmNotifier {

    /**
     * 告警发送通知
     */
    void notice(AlarmMessage message);

}
