package com.github.ltsopensource.autoconfigure.resolver;

import com.github.ltsopensource.autoconfigure.AutoConfigContext;
import com.github.ltsopensource.autoconfigure.PropertiesConfigurationFactory;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 4/20/16.
 */
public class JavaBeanResolver extends AbstractResolver {

    public static final JavaBeanResolver INSTANCE = new JavaBeanResolver();

    @Override
    public void resolve(AutoConfigContext context, PropertyDescriptor descriptor, Class<?> propertyType) {

        final Map<String, String> includeMap = new HashMap<String, String>();

        doFilter(context, descriptor, new Filter() {
            @Override
            public boolean onCondition(String name, String key, String value) {
                return key.startsWith(name);
            }

            @Override
            public boolean call(String name, String key, String value) {
                String subKey = key.substring(name.length() + 1);
                includeMap.put(subKey, value);
                return true;
            }
        });

        Object value = PropertiesConfigurationFactory.createPropertiesConfiguration(propertyType, null, includeMap);
        if (value != null) {
            writeProperty(context, descriptor, value);
        }
    }
}
