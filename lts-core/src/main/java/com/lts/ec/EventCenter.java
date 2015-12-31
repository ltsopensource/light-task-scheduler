package com.lts.ec;

import com.lts.core.spi.SPI;
import com.lts.core.spi.SpiKey;

/**
 * 事件中心接口
 *
 * @author Robert HG (254963746@qq.com) on 5/11/15.
 */
@SPI(key = SpiKey.EVENT_CENTER, dftValue = "injvm")
public interface EventCenter {

    /**
     * 订阅主题
     */
    public void subscribe(EventSubscriber subscriber, String... topics);

    /**
     * 取消订阅主题
     */
    public void unSubscribe(String topic, EventSubscriber subscriber);

    /**
     * 同步发布主题消息
     */
    public void publishSync(EventInfo eventInfo);

    /**
     * 异步发送主题消息
     */
    public void publishAsync(EventInfo eventInfo);

}
