package com.lts.job.ec.injvm;

import com.lts.job.core.constant.Constants;
import com.lts.job.core.util.ConcurrentHashSet;
import com.lts.job.core.util.JSONUtils;
import com.lts.job.core.logger.Logger;
import com.lts.job.core.logger.LoggerFactory;
import com.lts.job.ec.EventCenter;
import com.lts.job.ec.EventInfo;
import com.lts.job.ec.EventSubscriber;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 在一个jvm中的pub sub 简易实现
 * @author Robert HG (254963746@qq.com) on 5/12/15.
 */
public class InjvmEventCenter implements EventCenter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventCenter.class.getName());

    private Map<String, Set<EventSubscriber>> ecMap =
            new ConcurrentHashMap<String, Set<EventSubscriber>>();

    private ExecutorService executor = Executors.newFixedThreadPool(Constants.AVAILABLE_PROCESSOR * 2);

    public void subscribe(String topic, EventSubscriber subscriber) {
        Set<EventSubscriber> subscribers = ecMap.get(topic);
        if (subscribers == null) {
            synchronized (ecMap) {
                subscribers = new ConcurrentHashSet<EventSubscriber>();
                ecMap.put(topic, subscribers);
            }
        }
        subscribers.add(subscriber);
    }

    public void subscribe(String[] topics, EventSubscriber subscriber) {
        for (String topic : topics) {
            subscribe(topic, subscriber);
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
                subscriber.getObserver().onObserved(eventInfo);
            }
        }
    }

    public void publishAsync(final EventInfo eventInfo) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                String topic = eventInfo.getTopic();
                try {
                    Set<EventSubscriber> subscribers = ecMap.get(topic);
                    if (subscribers != null) {
                        for (EventSubscriber subscriber : subscribers) {
                            eventInfo.setTopic(topic);
                            subscriber.getObserver().onObserved(eventInfo);
                        }
                    }
                } catch (Throwable t) {
                    LOGGER.error("topic:{}, eventInfo:{}", topic, JSONUtils.toJSONString(eventInfo), t);
                }
            }
        });
    }
}
