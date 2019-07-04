package com.github.ltsopensource.core.spi;


import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.util.Assert;

/**
 * @author Robert HG (254963746@qq.com)on 12/23/15.
 */
public final class ClassLoaderUtil {

    private static final ConstructorCache CONSTRUCTOR_CACHE = new ConstructorCache();

    private ClassLoaderUtil() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(ClassLoader classLoader, final String className) throws Exception {
        classLoader = classLoader == null ? ClassLoaderUtil.class.getClassLoader() : classLoader;
        Constructor<T> constructor = CONSTRUCTOR_CACHE.get(classLoader, className);
        if (constructor != null) {
            return constructor.newInstance();
        }
        Class<T> clazz = (Class<T>) loadClass(classLoader, className);
        return (T) newInstance(clazz, classLoader, className);
    }

    public static <T> T newInstance(Class<T> clazz, ClassLoader classLoader, String className) throws Exception {
        final Constructor<T> constructor = clazz.getDeclaredConstructor();
        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
        CONSTRUCTOR_CACHE.put(classLoader, className, constructor);
        return constructor.newInstance();
    }

    public static Class<?> loadClass(final ClassLoader classLoader, final String className)
            throws ClassNotFoundException {

        Assert.notNull(className, "className");

        ClassLoader theClassLoader = classLoader;
        if (theClassLoader == null) {
            theClassLoader = Thread.currentThread().getContextClassLoader();
        }

        if (theClassLoader != null) {
            try {
                return tryLoadClass(className, theClassLoader);
            } catch (ClassNotFoundException ignore) {
            }
        }
        return Class.forName(className);
    }

    private static Class<?> tryLoadClass(String className, ClassLoader classLoader)
            throws ClassNotFoundException {

        if (className.startsWith("[")) {
            return Class.forName(className, false, classLoader);
        } else {
            return classLoader.loadClass(className);
        }
    }

    private static final class ConstructorCache {
        private final ConcurrentMap<ClassLoader, ConcurrentMap<String, WeakReference<Constructor>>> cache;

        private ConstructorCache() {
            cache = new ConcurrentHashMap<ClassLoader, ConcurrentMap<String, WeakReference<Constructor>>>();
        }

        private <T> Constructor put(ClassLoader classLoader, String className, Constructor<T> constructor) {
            ClassLoader cl = classLoader == null ? ClassLoaderUtil.class.getClassLoader() : classLoader;
            ConcurrentMap<String, WeakReference<Constructor>> innerCache = cache.get(cl);
            if (innerCache == null) {
                innerCache = new ConcurrentHashMap<String, WeakReference<Constructor>>(100);
                ConcurrentMap<String, WeakReference<Constructor>> old = cache.putIfAbsent(cl, innerCache);
                if (old != null) {
                    innerCache = old;
                }
            }
            innerCache.put(className, new WeakReference<Constructor>(constructor));
            return constructor;
        }

        @SuppressWarnings("unchecked")
        public <T> Constructor<T> get(ClassLoader classLoader, String className) {
            Assert.notNull(className, "className");
            ConcurrentMap<String, WeakReference<Constructor>> innerCache = cache.get(classLoader);
            if (innerCache == null) {
                return null;
            }
            WeakReference<Constructor> reference = innerCache.get(className);
            Constructor constructor = reference == null ? null : reference.get();
            if (reference != null && constructor == null) {
                innerCache.remove(className);
            }
            return (Constructor<T>) constructor;
        }
    }
}
