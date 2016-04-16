package com.github.ltsopensource.spring.quartz;

import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;

/**
 * @author Robert HG (254963746@qq.com) on 3/16/16.
 */
class QuartzSchedulerBeanRegistrar implements PropertyEditorRegistrar {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzSchedulerBeanRegistrar.class);

    private static final String SchedulerFactoryBean = "org.springframework.scheduling.quartz.SchedulerFactoryBean";

    private QuartzProxyContext context;

    public QuartzSchedulerBeanRegistrar(QuartzProxyContext context) {
        this.context = context;
    }

    @Override
    public void registerCustomEditors(PropertyEditorRegistry registry) {
        if (!(registry instanceof BeanWrapperImpl)) {
            return;
        }

        BeanWrapperImpl beanWrapper = (BeanWrapperImpl) registry;

        Class<?> clazz = null;
        try {
            clazz = Class.forName(SchedulerFactoryBean, true, registry.getClass().getClassLoader());
        } catch (Throwable e) {
            LOGGER.info("cannot find class for " + SchedulerFactoryBean, e);
        }

        if (null == clazz
                || null == beanWrapper.getWrappedClass()
                || !clazz.isAssignableFrom(beanWrapper.getWrappedClass())) {
            return;
        }

        registry.registerCustomEditor(Object.class, "triggers",
                new QuartzSchedulerBeanTargetEditor(context));
    }
}
