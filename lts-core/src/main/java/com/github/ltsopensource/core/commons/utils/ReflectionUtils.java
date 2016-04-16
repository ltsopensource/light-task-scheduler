package com.github.ltsopensource.core.commons.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 */
public class ReflectionUtils {

    /**
     * 得到所有field , 包括 父类
     */
    public static Field[] findFields(Class<?> clazz) throws IllegalAccessException {
        final List<Field> fieldList = new ArrayList<Field>();

        doWithDeclaredFields(clazz, new FieldCallback() {
            public void doWith(Field field) {
                fieldList.add(field);
            }
        });
        return fieldList.toArray(new Field[fieldList.size()]);
    }

    public static Field findField(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return findField(clazz.getSuperclass(), name);
            }
            return null;
        }
    }

    private static void doWithDeclaredFields(Class<?> clazz, FieldCallback fieldCallback) throws IllegalAccessException {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            fieldCallback.doWith(field);
        }
        if (clazz.getSuperclass() != null) {
            doWithDeclaredFields(clazz.getSuperclass(), fieldCallback);
        }
    }

    /**
     * 得到所有field , 包括 父类
     */
    public static Method[] findMethods(Class<?> clazz) throws IllegalAccessException {
        final List<Method> methodList = new ArrayList<Method>();

        doWithDeclaredMethods(clazz, new MethodCallback() {
            public void doWith(Method method) {
                methodList.add(method);
            }
        });
        return methodList.toArray(new Method[methodList.size()]);
    }

    public static Method findMethod(Class<?> clazz, String name, Class<?> parameterTypes) {
        try {
            if(parameterTypes != null){
                return clazz.getDeclaredMethod(name, parameterTypes);
            }else{
                return clazz.getDeclaredMethod(name);
            }
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() != null) {
                return findMethod(clazz.getSuperclass(), name, parameterTypes);
            }
            return null;
        }
    }

    public static Method findMethod(Class<?> clazz, String name) {
        return findMethod(clazz, name, null);
    }

    private static void doWithDeclaredMethods(Class<?> clazz, MethodCallback methodCallback) throws IllegalAccessException {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            methodCallback.doWith(method);
        }
        if (clazz.getSuperclass() != null) {
            doWithDeclaredMethods(clazz.getSuperclass(), methodCallback);
        }
    }

    private interface FieldCallback {

        void doWith(Field field) throws IllegalArgumentException, IllegalAccessException;
    }

    private interface MethodCallback {

        void doWith(Method method) throws IllegalArgumentException, IllegalAccessException;
    }
}
