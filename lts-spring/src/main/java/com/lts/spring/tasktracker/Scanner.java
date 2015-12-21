package com.lts.spring.tasktracker;

import com.lts.core.commons.utils.StringUtils;
import com.lts.core.domain.Job;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.tasktracker.Result;
import com.lts.tasktracker.runner.JobRunner;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * @author Robert HG (254963746@qq.com) on 10/20/15.
 */
public class Scanner implements DisposableBean, BeanFactoryPostProcessor, BeanPostProcessor{

    private static final Logger LOGGER = LoggerFactory.getLogger(Scanner.class);

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
                Object filter = filterClass.getConstructor(Class.class).newInstance(LTS.class);
                Method addIncludeFilter = scannerClass.getMethod("addIncludeFilter", Class.forName("org.springframework.core.type.filter.TypeFilter"));
                addIncludeFilter.invoke(scanner, filter);
                // scan packages
                Method scan = scannerClass.getMethod("scan", String[].class);
                scan.invoke(scanner, new Object[]{annotationPackages});
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {

        Class<?> clazz = bean.getClass();

        if (!isMatchPackage(clazz)) {
            return bean;
        }

        if (!clazz.isAnnotationPresent(LTS.class)) {
            return bean;
        }

        Method[] methods = clazz.getMethods();
        if (methods != null && methods.length > 0) {

            for (final Method method : methods) {
                if (method.isAnnotationPresent(JobRunnerItem.class)) {
                    JobRunnerItem jobRunnerItem = method.getAnnotation(JobRunnerItem.class);
                    String shardValue = jobRunnerItem.shardValue();
                    if (StringUtils.isEmpty(shardValue)) {
                        LOGGER.error(clazz.getName() + ":" + method.getName() + " " + JobRunnerItem.class.getName() + " shardValue can not be null");
                        continue;
                    }
                    Class<?> returnType = method.getReturnType();
                    if (returnType != Result.class) {
                        LOGGER.error(clazz.getName() + ":" + method.getName() + " returnType must be " + Result.class.getName());
                        continue;
                    }

                    final Class<?>[] pTypes = method.getParameterTypes();

                    JobRunnerHolder.add(shardValue, new JobRunner() {
                        @Override
                        public Result run(Job job) throws Throwable {
                            if (pTypes == null || pTypes.length == 0) {
                                return (Result) method.invoke(bean);
                            }
                            Object[] pTypeValues = new Object[pTypes.length];

                            for (int i = 0; i < pTypes.length; i++) {
                                if (pTypes[i] == Job.class) {
                                    pTypeValues[i] = job;
                                } else {
                                    pTypeValues[i] = null;
                                }
                            }
                            return (Result) method.invoke(bean, pTypeValues);
                        }
                    });
                }
            }
        }

        return bean;
    }

    @Override
    public void destroy() throws Exception {

    }

    private boolean isMatchPackage(Class<?> clazz) {
        if (annotationPackages == null || annotationPackages.length == 0) {
            return true;
        }
        String beanClassName = clazz.getName();
        for (String pkg : annotationPackages) {
            if (beanClassName.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }
}
