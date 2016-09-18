package com.github.ltsopensource.core.support.bean;

/**
 * @author Robert HG (254963746@qq.com) on 4/2/16.
 */
public interface BeanCopier<Source, Target> {

    /**
     * 拷贝属性
     */
    void copyProps(Source source, Target target);

}
