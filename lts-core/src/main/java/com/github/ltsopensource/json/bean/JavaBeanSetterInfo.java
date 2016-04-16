package com.github.ltsopensource.json.bean;

import java.lang.reflect.*;
import java.security.AccessControlException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 12/31/15.
 */
public class JavaBeanSetterInfo {

    private Class<?> clazz;

    private Constructor<?> constructor;

    private Map<String, MethodInfo> methodSetterInfos;

    private Map<String, FieldSetterInfo> fieldSetterInfos;

    public JavaBeanSetterInfo(Class<?> clazz) {
        this.clazz = clazz;

        Constructor<?> constructor = getDefaultConstructor(clazz);
        setAccessible(constructor);
        this.constructor = constructor;

        this.methodSetterInfos = getMethodSettInfos();
        this.fieldSetterInfos = getFiledSetterInfos();
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public Collection<MethodInfo> getMethodSetterInfos() {
        if (methodSetterInfos == null) {
            return null;
        }
        return methodSetterInfos.values();
    }


    public Collection<FieldSetterInfo> getFieldSetterInfos() {
        if (fieldSetterInfos == null) {
            return null;
        }
        return fieldSetterInfos.values();
    }

    private Constructor<?> getDefaultConstructor(Class<?> clazz) {
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return null;
        }

        Constructor<?> defaultConstructor = null;
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterTypes().length == 0) {
                defaultConstructor = constructor;
                break;
            }
        }

        if (defaultConstructor == null) {
            if (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
                for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                    if (constructor.getParameterTypes().length == 1
                            && constructor.getParameterTypes()[0].equals(clazz.getDeclaringClass())) {
                        defaultConstructor = constructor;
                        break;
                    }
                }
            }
        }
        return defaultConstructor;
    }

    private void setAccessible(AccessibleObject obj) {
        if (obj.isAccessible()) {
            return;
        }
        try {
            obj.setAccessible(true);
        } catch (AccessControlException ignored) {
        }
    }

    private Map<String, MethodInfo> getMethodSettInfos() {

        Map<String, MethodInfo> map = new HashMap<String, MethodInfo>();

        for (Method method : clazz.getMethods()) {

            String methodName = method.getName();
            if (methodName.length() < 4) {
                continue;
            }

            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            // support builder set
            if (!(method.getReturnType().equals(Void.TYPE) || method.getReturnType().equals(clazz))) {
                continue;
            }

            if (method.getParameterTypes().length != 1) {
                continue;
            }

            if (!methodName.startsWith("set")) {
                continue;
            }

            char c3 = methodName.charAt(3);

            String propertyName;
            if (Character.isUpperCase(c3)) {
                propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
            } else if (c3 == '_') {
                propertyName = methodName.substring(4);
            } else if (c3 == 'f') {
                propertyName = methodName.substring(3);
            } else if (methodName.length() >= 5 && Character.isUpperCase(methodName.charAt(4))) {
                propertyName = decapitalize(methodName.substring(3));
            } else {
                continue;
            }

            Field field = getField(clazz, propertyName);
            if (field == null && method.getParameterTypes()[0] == boolean.class) {
                String isFieldName = "is" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
                field = getField(clazz, isFieldName);
            }
            if (field != null) {
                // 说明该方法是存在的
                map.put(propertyName, new MethodInfo(propertyName, method));
            }
        }
        return map;
    }

    /**
     * 返回所有的public方法
     */
    public Map<String, FieldSetterInfo> getFiledSetterInfos() {

        Map<String, FieldSetterInfo> map = new HashMap<String, FieldSetterInfo>();

        for (Field field : clazz.getFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            if (Modifier.isFinal(field.getModifiers())) {
                continue;
            }

            if (!Modifier.isPublic(field.getModifiers())) {
                continue;
            }
            String propertyName = field.getName();

            // 如果已经暴露了方法,那么就不使用属性设置
            if (this.methodSetterInfos.containsKey(propertyName)) {
                continue;
            }
            map.put(propertyName, new FieldSetterInfo(propertyName, field));
        }
        return map;
    }

    private String decapitalize(String name) {
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) &&
                Character.isUpperCase(name.charAt(0))) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    private Field getField(Class<?> clazz, String fieldName) {
        for (Field field : clazz.getDeclaredFields()) {
            if (fieldName.equals(field.getName())) {
                return field;
            }
        }

        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            return getField(superClass, fieldName);
        }

        return null;
    }
}
