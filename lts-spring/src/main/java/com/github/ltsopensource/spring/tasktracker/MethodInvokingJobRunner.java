package com.github.ltsopensource.spring.tasktracker;

import com.github.ltsopensource.core.commons.utils.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.Method;

/**
 * @author Robert HG (254963746@qq.com)on 12/21/15.
 */
public class MethodInvokingJobRunner implements InitializingBean {

    private Object targetObject;
    private String targetMethod;
    private String shardValue;

    @Override
    public void afterPropertiesSet() throws Exception {

        if (targetObject == null) {
            throw new IllegalArgumentException("targetObject can not be null");
        }
        if (StringUtils.isEmpty(targetMethod)) {
            throw new IllegalArgumentException("targetMethod can not be null");
        }
        if (StringUtils.isEmpty(shardValue)) {
            throw new IllegalArgumentException("shardValue can not be null");
        }

        Class<?> clazz = targetObject.getClass();
        Method[] methods = clazz.getMethods();
        Method method = null;
        if (methods != null && methods.length > 0) {
            for (Method m : methods) {
                if (m.getName().equals(targetMethod)) {
                    if (method != null) {
                        throw new IllegalArgumentException("Duplicate targetMethod can not be found in " + targetObject.getClass().getName());
                    }
                    method = m;
                }
            }
        }

        if (method == null) {
            throw new IllegalArgumentException("targetMethod can not be found in " + targetObject.getClass().getName());
        }

        JobRunnerHolder.add(shardValue, JobRunnerBuilder.build(targetObject, method, method.getParameterTypes()));

    }

    public void setTargetObject(Object targetObject) {
        this.targetObject = targetObject;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public void setShardValue(String shardValue) {
        this.shardValue = shardValue;
    }

}
