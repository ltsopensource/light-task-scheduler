package com.github.ltsopensource.alarm;

/**
 * 要保证同一条消息不会被重复发送多次
 * @author Robert HG (254963746@qq.com)  on 2/17/16.
 */
public abstract class AbstractAlarmNotifier<T extends AlarmMessage> implements AlarmNotifier<T> {
    @Override
    public final void notice(T message) {
        // TODO

        doNotice(message);
    }

    protected abstract void doNotice(T message);
}
