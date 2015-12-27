package com.lts.ec;

import com.lts.core.spi.SPI;
import com.lts.core.spi.SKey;

/**
 * 事件中心接口
 *
 * @author Robert HG (254963746@qq.com) on 5/11/15.
 */
@SPI(key = SKey.EVENT_CENTER, dftValue = "injvm")
public interface EventCenter {

    /**
     * 订阅主题
     *
     * @param topics
     * @param subscriber
     */
    public void subscribe(EventSubscriber subscriber, String... topics);

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
