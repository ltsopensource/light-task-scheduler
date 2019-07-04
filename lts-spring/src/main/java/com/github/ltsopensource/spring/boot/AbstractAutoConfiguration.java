package com.github.ltsopensource.spring.boot;

import com.github.ltsopensource.core.cluster.NodeType;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@Log4j2
public abstract class AbstractAutoConfiguration implements ApplicationContextAware, InitializingBean, DisposableBean {

    protected ApplicationContext applicationContext;

    @Override
    public final void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public final void afterPropertiesSet() throws Exception {
        initJobNode();
    }

    @Override
    public final void destroy() throws Exception {
    }

    protected abstract void initJobNode();

    protected abstract NodeType nodeType();
}
