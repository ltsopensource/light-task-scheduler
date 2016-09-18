package com.github.ltsopensource.json.bean;

import java.lang.reflect.Method;

/**
 * @author Robert HG (254963746@qq.com) on 12/31/15.
 */
public class MethodInfo {

    private String fieldName;

    private Method method;

    public MethodInfo(String fieldName, Method method) {
        this.fieldName = fieldName;
        this.method = method;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
