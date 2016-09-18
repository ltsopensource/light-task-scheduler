package com.github.ltsopensource.alarm;

/**
 * @author Robert HG (254963746@qq.com)  on 2/17/16.
 */
public interface AlarmNotifier<T extends AlarmMessage> {

    /**
     * 告警发送通知
     */
    void notice(T message);

}
