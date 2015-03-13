package com.lts.job.core.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 *         Bean 单利 context
 */
public class SingletonBeanContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingletonBeanContext.class);
    // 用于存放bean的map
    private static final ConcurrentHashMap<Class, Object> beanMap = new ConcurrentHashMap<Class, Object>();
    // 同一个时间只能有一个线程在创建bean
    private static Object lock = new Object();

    public static <T> T getBean(Class clazz) {
        Object bean = beanMap.get(clazz);
        if (bean == null) {
            synchronized (lock) {
                if (bean != null) {
                    return (T) bean;
                }
                try {
                    bean = clazz.newInstance();
                    if (bean instanceof Singleton) {
                        beanMap.put(clazz, bean);
                    } else {
                        LOGGER.error("{} is not implements {}", clazz.getName(), Singleton.class.getName());
                    }
                } catch (InstantiationException e) {
                    LOGGER.error(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        return (T) bean;
    }

}
