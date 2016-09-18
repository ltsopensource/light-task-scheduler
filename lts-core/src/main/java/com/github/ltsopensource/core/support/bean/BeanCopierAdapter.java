package com.github.ltsopensource.core.support.bean;

/**
 * @author Robert HG (254963746@qq.com) on 4/17/16.
 */
public abstract class BeanCopierAdapter implements BeanCopier<Object, Object> {

    public abstract void copyProps(Object sourceObj, Object targetObj);
}
