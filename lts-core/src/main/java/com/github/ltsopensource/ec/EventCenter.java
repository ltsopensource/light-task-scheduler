package com.github.ltsopensource.ec;

import com.github.ltsopensource.core.spi.SPI;
import com.github.ltsopensource.core.constant.ExtConfig;

/**
 * 事件中心接口
 *
 * @author Robert HG (254963746@qq.com) on 5/11/15.
 */
@SPI(key = ExtConfig.EVENT_CENTER, dftValue = "injvm")
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
