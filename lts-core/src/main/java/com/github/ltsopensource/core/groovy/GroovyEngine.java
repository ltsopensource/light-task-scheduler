package com.github.ltsopensource.core.groovy;

import groovy.lang.GroovyClassLoader;

import java.lang.reflect.Method;

/**
 * @author Robert HG (254963746@qq.com) on 11/11/15.
 */
public class GroovyEngine {

    private GroovyEngine() {
    }

    /**
     * 将groovy源码解析为Class
     */
    public static Class parseClass(String groovySource) throws GroovyException {

        GroovyClassLoader loader = new GroovyClassLoader();

        ClassLoader contextClassLoader = null;

        try {
            contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (contextClassLoader != null) {
                Thread.currentThread().setContextClassLoader(null);
            }
            return loader.parseClass(groovySource);
        } catch (Throwable t) {
            throw new GroovyException("parseClass error:", t);
        } finally {
            if (contextClassLoader != null) {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
            }
        }
    }

    /**
     * 实例化Groovy对象
     */
    public static Object newInstance(String groovySource,
                                     Class<?>[] parameterTypes,
                                     Object[] args) throws GroovyException {

        Class<?> groovyClass = parseClass(groovySource);
        try {
            if (parameterTypes == null || parameterTypes.length == 0) {
                return groovyClass.getConstructor().newInstance();
            }
            return groovyClass.getConstructor(parameterTypes).newInstance(args);

        } catch (Throwable t) {
            throw new GroovyException("newInstance error:", t);
        }
    }

    public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes)
            throws GroovyException {
        try {
            return clazz.getMethod(name, parameterTypes);
        } catch (Throwable t) {
            throw new GroovyException("Get Method " + name + " error:", t);
        }
    }

    public static Object invokeMethod(Object obj, Method m, Object... args)
            throws GroovyException {
        try {
            return m.invoke(obj, args);
        } catch (Throwable t) {
            throw new GroovyException("Invoke Method " + m.getName() + " error:", t);
        }
    }

    public static Object invokeMethod(Object obj,
                                      Class<?> clazz,
                                      String name,
                                      Class<?>[] parameterTypes,
                                      Object[] args)
            throws GroovyException {

        try {
            Method method = null;
            if (parameterTypes == null) {
                method = getMethod(clazz, name);
            } else {
                method = getMethod(clazz, name, parameterTypes);
            }
            if (args == null) {
                return method.invoke(obj);
            }
            return method.invoke(obj, args);
        } catch (Throwable t) {
            throw new GroovyException("Invoke Method " + name + " error:", t);
        }
    }
}
