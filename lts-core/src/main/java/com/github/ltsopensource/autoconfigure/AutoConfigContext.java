package com.github.ltsopensource.autoconfigure;

import java.beans.PropertyDescriptor;
import java.util.Map;
import java.util.Set;

/**
 * @author Robert HG (254963746@qq.com) on 4/20/16.
 */
public class AutoConfigContext {

    private Map<String, PropertyDescriptor> nameDescriptorMap;
    private Map<PropertyDescriptor, Set<String>> descriptorNamesMap;
    private Map<String, String> propMap;
    private Object targetObject;

    public AutoConfigContext(Map<String, PropertyDescriptor> nameDescriptorMap, Map<PropertyDescriptor, Set<String>> descriptorNamesMap, Map<String, String> propMap, Object targetObject) {
        this.nameDescriptorMap = nameDescriptorMap;
        this.descriptorNamesMap = descriptorNamesMap;
        this.propMap = propMap;
        this.targetObject = targetObject;
    }

    public Set<String> getNamesByDescriptor(PropertyDescriptor descriptor) {
        if (descriptorNamesMap == null) {
            return null;
        }
        return descriptorNamesMap.get(descriptor);
    }

    public Map<String, PropertyDescriptor> getNameDescriptorMap() {
        return nameDescriptorMap;
    }

    public Map<String, String> getPropMap() {
        return propMap;
    }

    public Object getTargetObject() {
        return targetObject;
    }
}
