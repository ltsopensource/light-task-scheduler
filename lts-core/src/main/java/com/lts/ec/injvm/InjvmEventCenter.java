package com.lts.ec.injvm;

import com.lts.core.commons.collect.ConcurrentHashSet;
import com.lts.core.commons.utils.JSONUtils;
import com.lts.core.constant.Constants;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.ec.EventCenter;
import com.lts.ec.EventInfo;
import com.lts.ec.EventSubscriber;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 在一个jvm中的pub sub 简易实现
 *
 * @author Robert HG (254963746@qq.com) on 5/12/15.
 */
public class InjvmEventCenter implements EventCenter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventCenter.class.getName());

    private final ConcurrentHashMap<String, Set<EventSubscriber>> ecMap =
            new ConcurrentHashMap<String, Set<EventSubscriber>>();

    private final ExecutorService executor = Executors.newFixedThreadPool(Constants.AVAILABLE_PROCESSOR * 2);

    public void subscribe(EventSubscriber subscriber, String... topics) {
        for (String topic : topics) {
            Set<EventSubscriber> subscribers = ecMap.get(topic);
            if (subscribers == null) {
                subscribers = new ConcurrentHashSet<EventSubscriber>();
                Set<EventSubscriber> oldSubscribers = ecMap.putIfAbsent(topic, subscribers);
                if (oldSubscribers != null) {
                    subscribers = oldSubscribers;
                }
            }
            subscribers.add(subscriber);
        }
    }

    public void unSubscribe(String topic, EventSubscriber subscriber) {
        Set<EventSubscriber> subscribers = ecMap.get(topic);
        if (subscribers != null) {
            for (EventSubscriber eventSubscriber : subscribers) {
                if (eventSubscriber.getId().equals(subscriber.getId())) {
                    subscribers.remove(eventSubscriber);
                }
            }
        }
    }

    public void publishSync(EventInfo eventInfo) {
        Set<EventSubscriber> subscribers = ecMap.get(eventInfo.getTopic());
        if (subscribers != null) {
            for (EventSubscriber subscriber : subscribers) {
                eventInfo.setTopic(eventInfo.getTopic());
                try {
                    subscriber.getObserver().onObserved(eventInfo);
                } catch (Throwable t) {      // 防御性容错
                    LOGGER.error(" eventInfo:{}, subscriber:{}",
                            JSONUtils.toJSONString(eventInfo), JSONUtils.toJSONString(subscriber), t);
                }
            }
        }
    }

    public void publishAsync(final EventInfo eventInfo) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                String topic = eventInfo.getTopic();

                Set<EventSubscriber> subscribers = ecMap.get(topic);
                if (subscribers != null) {
                    for (EventSubscriber subscriber : subscribers) {
                        try {
                            eventInfo.setTopic(topic);
                            subscriber.getObserver().onObserved(eventInfo);
                        } catch (Throwable t) {     // 防御性容错
                            LOGGER.error(" eventInfo:{}, subscriber:{}",
                                    JSONUtils.toJSONString(eventInfo), JSONUtils.toJSONString(subscriber), t);
                        }
                    }
                }
            }
        });
    }
}
