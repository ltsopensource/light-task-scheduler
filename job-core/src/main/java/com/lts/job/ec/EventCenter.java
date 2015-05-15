package com.lts.job.ec;

/**
 * 事件中心接口
 * Created by hugui on 5/11/15.
 */
public interface EventCenter {

    /**
     * 订阅主题
     *
     * @param topic
     * @param subscriber
     */
    public void subscribe(String topic, EventSubscriber subscriber);

    /**
     * 订阅主题
     *
     * @param topics
     * @param subscriber
     */
    public void subscribe(String[] topics, EventSubscriber subscriber);

    /**
     * 取消订阅主题
     *
     * @param topic
     * @param subscriber
     */
    public void unSubscribe(String topic, EventSubscriber subscriber);

    /**
     * 同步发布主题消息
     *
     * @param eventInfo
     */
    public void publishSync(EventInfo eventInfo);

    /**
     * 异步发送主题消息
     *
     * @param eventInfo
     */
    public void publishAsync(EventInfo eventInfo);

}
