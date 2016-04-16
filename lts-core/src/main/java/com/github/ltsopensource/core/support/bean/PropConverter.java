package com.github.ltsopensource.core.support.bean;

/**
 * @author Robert HG (254963746@qq.com) on 4/2/16.
 */
public interface PropConverter<Source, Output> {

    /**
     * @param source 是原对象
     * @return 这个属性的值
     */
    Output convert(Source source);
}
