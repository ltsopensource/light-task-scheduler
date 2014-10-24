package com.lts.job.common.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 * Bean 单利 context
 */
public class SingletonBeanContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingletonBeanContext.class);
    // 用于存放bean的map
    private static final ConcurrentHashMap<Class, Object> beanMap = new ConcurrentHashMap<Class, Object>();
    // 同一个时间只能有一个线程在创建bean
    private static Lock lock = new ReentrantLock();

    public static <T> T getBean(Class clazz) {
        Object bean = beanMap.get(clazz);
        if (bean == null) {
            lock.lock();
            try {
                if (bean != null) {
                    return (T)bean;
                }
                try {
                    bean = clazz.newInstance();
                    beanMap.put(clazz, bean);
                } catch (InstantiationException e) {
                    LOGGER.error(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            } finally {
                lock.unlock();
            }
        }
        return (T) bean;
    }

}
