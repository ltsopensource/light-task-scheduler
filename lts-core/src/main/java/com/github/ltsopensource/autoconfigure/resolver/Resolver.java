package com.github.ltsopensource.autoconfigure.resolver;

import com.github.ltsopensource.autoconfigure.AutoConfigContext;

import java.beans.PropertyDescriptor;

/**
 * @author Robert HG (254963746@qq.com) on 4/20/16.
 */
public interface Resolver {

    void resolve(AutoConfigContext context, PropertyDescriptor descriptor, Class<?> propertyType);

}
