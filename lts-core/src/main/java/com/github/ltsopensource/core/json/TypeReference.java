package com.github.ltsopensource.core.json;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author Robert HG (254963746@qq.com) on 11/19/15.
 */
public abstract class TypeReference<T> {

    private final Type type;

    public TypeReference() {
        Type superClass = getClass().getGenericSuperclass();

        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    public Type getType() {
        return type;
    }

}