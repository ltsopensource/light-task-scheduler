package com.lts.spring.tasktracker;

import com.lts.core.commons.utils.StringUtils;
import com.lts.tasktracker.runner.JobRunner;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @author Robert HG (254963746@qq.com) on 10/20/15.
 */
public class LtsJobRunnerScanner implements DisposableBean, BeanFactoryPostProcessor, BeanPostProcessor {

    private String[] annotationPackages;

    public void setBasePackage(String annotationPackage) {
        this.annotationPackages = (annotationPackage == null || annotationPackage.length() == 0) ? null
                : Pattern.compile("\\s*[,]+\\s*").split(annotationPackage);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        if (beanFactory instanceof BeanDefinitionRegistry) {
            try {
                // init scanner
                Class<?> scannerClass = Class.forName("org.springframework.context.annotation.ClassPathBeanDefinitionScanner");
                Object scanner = scannerClass.getConstructor(new Class<?>[]{BeanDefinitionRegistry.class, boolean.class}).newInstance(beanFactory, true);
                // add filter
                Class<?> filterClass = Class.forName("org.springframework.core.type.filter.AnnotationTypeFilter");
                Object filter = filterClass.getConstructor(Class.class).newInstance(LtsJobRunner.class);
                Method addIncludeFilter = scannerClass.getMethod("addIncludeFilter", Class.forName("org.springframework.core.type.filter.TypeFilter"));
                addIncludeFilter.invoke(scanner, filter);
                // scan packages
                Method scan = scannerClass.getMethod("scan", String[].class);
                scan.invoke(scanner, new Object[]{annotationPackages});
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!isMatchPackage(bean)) {
            return bean;
        }
        LtsJobRunner jobRunner = bean.getClass().getAnnotation(LtsJobRunner.class);
        if (jobRunner != null) {
            if (bean instanceof JobRunner) {
                String shardValue = jobRunner.value();
                if (StringUtils.isEmpty(shardValue)) {
                    // 这个默认流量
                    shardValue = "_LTS_DEFAULT";
                }
                if (jobRunnerMap.get(shardValue) != null) {
                    throw new IllegalStateException("Duplicate LtsJobRunner ShardValue : " + shardValue);
                }
                jobRunnerMap.put(shardValue, (JobRunner) bean);
            }
        }
        return bean;
    }

    private Map<String, JobRunner> jobRunnerMap = new ConcurrentHashMap<String, JobRunner>();

    protected JobRunner getJobRunner(String type) {
        return jobRunnerMap.get(type);
    }

    @Override
    public void destroy() throws Exception {

    }

    private boolean isMatchPackage(Object bean) {
        if (annotationPackages == null || annotationPackages.length == 0) {
            return true;
        }
        String beanClassName = bean.getClass().getName();
        for (String pkg : annotationPackages) {
            if (beanClassName.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

}
