package com.lts.alarm;

/**
 * 要保证同一条消息不会被重复发送多次
 * @author Robert HG (254963746@qq.com)  on 2/17/16.
 */
public abstract class AbstractAlarmNotifier implements AlarmNotifier {
    @Override
    public void notice(AlarmMessage message) {

    }
}
