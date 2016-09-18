package com.github.ltsopensource.autoconfigure.resolver;

import com.github.ltsopensource.autoconfigure.AutoConfigContext;
import com.github.ltsopensource.autoconfigure.PropertiesConfigurationResolveException;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * @author Robert HG (254963746@qq.com) on 4/20/16.
 */
public abstract class AbstractResolver implements Resolver {

    protected void doFilter(AutoConfigContext context, PropertyDescriptor descriptor, Filter filter) {

        Set<String> names = context.getNamesByDescriptor(descriptor);

        for (Map.Entry<String, String> entry : context.getPropMap().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            for (String name : names) {
                if (filter.onCondition(name, key, value)) {
                    filter.call(name, key, value);
                }
            }
        }
    }

    protected void writeProperty(AutoConfigContext context, PropertyDescriptor descriptor, Object value) {
        try {
            Method writeMethod = descriptor.getWriteMethod();
            writeMethod.invoke(context.getTargetObject(), value);
        } catch (Exception e) {
            throw new PropertiesConfigurationResolveException("Inject Property " + descriptor.getName() + " Error", e);
        }
    }

    protected interface Filter {

        boolean onCondition(String name, String key, String value);

        /**
         * @return is go on
         */
        boolean call(String name, String key, String value);
    }
}
