package com.github.ltsopensource.autoconfigure;

import com.github.ltsopensource.autoconfigure.annotation.ConfigurationProperties;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.GenericsUtils;
import com.github.ltsopensource.core.commons.utils.PrimitiveTypeUtils;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.domain.Pair;
import com.github.ltsopensource.core.json.JSON;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by hugui.hg on 4/18/16.
 */
@SuppressWarnings("unchecked")
public class PropertiesConfigurationFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesConfigurationFactory.class);

    public static <T> T createPropertiesConfiguration(Class<T> clazz, Map<String, String> propMap) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz should not be null");
        }
        if (!clazz.isAnnotationPresent(ConfigurationProperties.class)) {
            throw new IllegalArgumentException(clazz.getName() + " must annotation with @" + ConfigurationProperties.class.getName());
        }
        T targetObj;
        try {
            targetObj = clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(clazz.getName() + " instance error", e);
        }
        ConfigurationProperties annotation = clazz.getAnnotation(ConfigurationProperties.class);
        String prefix = annotation.prefix();

        List<PropertyDescriptor> propertyDescriptors = new ArrayList<PropertyDescriptor>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Introspector.IGNORE_ALL_BEANINFO);
            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                if (Class.class == clazz &&
                        ("classLoader".equals(pd.getName()) || "protectionDomain".equals(pd.getName()))) {
                    // Ignore Class.getClassLoader() and getProtectionDomain() methods - nobody needs to bind to those
                    continue;
                }
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Found bean property '" + pd.getName() + "'" +
                            (pd.getPropertyType() != null ? " of type [" + pd.getPropertyType().getName() + "]" : "") +
                            (pd.getPropertyEditorClass() != null ?
                                    "; editor [" + pd.getPropertyEditorClass().getName() + "]" : ""));
                }
                propertyDescriptors.add(pd);
            }

        } catch (IntrospectionException e) {
            throw new PropertiesConfigurationResolveException(e);
        }

        Pair<Map<String, PropertyDescriptor>, Map<PropertyDescriptor, Set<String>>> pair = getNames(prefix, propertyDescriptors);
        Map<String, PropertyDescriptor> nameDescriptorMap = pair.getKey();
        Map<PropertyDescriptor, Set<String>> descriptorNameMap = pair.getValue();

        Map<String, PropertyDescriptor> configNames = new LinkedHashMap<String, PropertyDescriptor>();
        if (CollectionUtils.isNotEmpty(nameDescriptorMap)) {
            for (String key : propMap.keySet()) {
                for (Map.Entry<String, PropertyDescriptor> entry : nameDescriptorMap.entrySet()) {
                    String name = entry.getKey();
                    if (key.startsWith(name)) {
                        configNames.put(key, entry.getValue());
                    }
                }
            }
        }

        for (Map.Entry<String, PropertyDescriptor> entry : configNames.entrySet()) {
            String name = entry.getKey();
            PropertyDescriptor descriptor = entry.getValue();
            Method writeMethod = descriptor.getWriteMethod();
            Method readMethod = descriptor.getReadMethod();
            Class<?> propertyType = descriptor.getPropertyType();

            if (Map.class.isAssignableFrom(propertyType)) {
                // Map
                Set<String> names = descriptorNameMap.get(descriptor);
                final Class<?> kClass = GenericsUtils.getMethodGenericReturnType(readMethod, 0);
                final Class<?> vClass = GenericsUtils.getMethodGenericReturnType(readMethod, 1);
                // k 必须是原始类型
                if (!PrimitiveTypeUtils.isPrimitiveClass(kClass)) {
                    throw new PropertiesConfigurationResolveException("Only support Map primitive type key");
                }
                Map map = new HashMap();

                for (Map.Entry<String, String> stringStringEntry : propMap.entrySet()) {
                    String key = stringStringEntry.getKey();
                    String value = stringStringEntry.getValue();
                    for (String s : names) {
                        if (key.startsWith(s)) {
                            String mapKey = key.substring(s.length() + 1);
                            if(PrimitiveTypeUtils.isPrimitiveClass(vClass)){
                                map.put(mapKey, PrimitiveTypeUtils.convert(value, vClass));
                            }else{
                                map.put(mapKey, JSON.parse(value, vClass));
                            }
                        }
                    }
                }
                System.out.println(map);
            }

            System.out.println(name + ":" + propertyType);
        }


        return targetObj;
    }

    private static Pair<Map<String, PropertyDescriptor>, Map<PropertyDescriptor, Set<String>>> getNames(String prefix, List<PropertyDescriptor> propertyDescriptors) {

        Iterable<String> prefixes = (StringUtils.hasLength(prefix)
                ? new RelaxedNames(prefix) : null);

        Map<String, PropertyDescriptor> nameDescriptorMap = new LinkedHashMap<String, PropertyDescriptor>();
        Map<PropertyDescriptor, Set<String>> descriptorNameMap = new HashMap<PropertyDescriptor, Set<String>>();
        for (PropertyDescriptor descriptor : propertyDescriptors) {
            String name = descriptor.getName();
            if (!name.equals("class")) {
                RelaxedNames relaxedNames = RelaxedNames.forCamelCase(name);
                if (prefixes == null) {
                    for (String relaxedName : relaxedNames) {
                        nameDescriptorMap.put(relaxedName, descriptor);
                        Set<String> names = CollectionUtils.newHashSetOnNull(descriptorNameMap.get(descriptor));
                        names.add(relaxedName);
                        if (!descriptorNameMap.containsKey(descriptor)) {
                            descriptorNameMap.put(descriptor, names);
                        }
                    }
                } else {
                    for (String _prefix : prefixes) {
                        for (String relaxedName : relaxedNames) {
                            String name1 = _prefix + "." + relaxedName;
                            String name2 = _prefix + "_" + relaxedName;
                            nameDescriptorMap.put(name1, descriptor);
                            nameDescriptorMap.put(name2, descriptor);

                            Set<String> names = CollectionUtils.newHashSetOnNull(descriptorNameMap.get(descriptor));
                            names.add(name1);
                            names.add(name2);
                            if (!descriptorNameMap.containsKey(descriptor)) {
                                descriptorNameMap.put(descriptor, names);
                            }
                        }
                    }
                }
            }
        }
        return new Pair<Map<String, PropertyDescriptor>, Map<PropertyDescriptor, Set<String>>>(nameDescriptorMap, descriptorNameMap);
    }

    public static <T> T createPropertiesConfiguration(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz should not be null");
        }
        if (!clazz.isAnnotationPresent(ConfigurationProperties.class)) {
            throw new IllegalArgumentException(clazz.getName() + " must annotation with @" + ConfigurationProperties.class.getName());
        }
        ConfigurationProperties annotation = clazz.getAnnotation(ConfigurationProperties.class);
        String[] locations = annotation.locations();
        if (locations == null || locations.length == 0) {
            throw new IllegalArgumentException(clazz.getName() + " must specified the properties locations");
        }
        Properties properties = new Properties();
        for (String location : locations) {
            try {
                properties.load(PropertiesConfigurationFactory.class.getClassLoader().getResourceAsStream(location.trim()));
            } catch (IOException e) {
                throw new IllegalStateException("Load properties [" + location + "] error", e);
            }
        }
        Map<String, String> map = new HashMap<String, String>(properties.size());
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue() == null ? null : entry.getValue().toString();
            if (value != null) {
                map.put(key, value);
            }
        }
        return createPropertiesConfiguration(clazz, map);
    }
}
