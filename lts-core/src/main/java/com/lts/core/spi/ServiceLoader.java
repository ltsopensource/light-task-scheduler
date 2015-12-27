package com.lts.core.spi;

import com.lts.core.cluster.Config;
import com.lts.core.commons.utils.Assert;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Robert HG (254963746@qq.com)on 12/23/15.
 */
public class ServiceLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLoader.class);
    private static final String LTS_DIRECTORY = "META-INF/lts/";
    private static final String LTS_INTERNAL = "internal";
    private static final String LTS_INTERNAL_DIRECTORY = LTS_DIRECTORY + LTS_INTERNAL + "/";

    private static final ConcurrentMap<Class<?>, ServiceProvider> serviceMap = new ConcurrentHashMap<Class<?>, ServiceProvider>();
    private static final ConcurrentMap<ServiceDefinition, Object> cachedObjectMap = new ConcurrentHashMap<ServiceDefinition, Object>();

    public static <T> T load(Class<T> clazz, Config config) {
        ServiceProvider serviceProvider = getServiceProvider(clazz);
        String dynamicServiceName = config.getParameter(serviceProvider.dynamicConfigKey);
        return load(clazz, dynamicServiceName);
    }

    public static <T> T loadDefault(Class<T> clazz) {
        return load(clazz, "");
    }

    @SuppressWarnings("unchecked")
    public static <T> T load(Class<T> clazz, String name) {
        try {
            ServiceProvider serviceProvider = getServiceProvider(clazz);
            if (StringUtils.isEmpty(name)) {
                // 加载默认的
                name = serviceProvider.defaultName;
            }
            ServiceDefinition definition = serviceProvider.nameMaps.get(name);
            if (definition == null) {
                throw new IllegalStateException("Service loader could not load name:" + name + "  class:" + clazz.getName() + "'s ServiceProvider from '" + LTS_DIRECTORY + "' or '" + LTS_INTERNAL_DIRECTORY + "' It may be empty or does not exist.");
            }

            Object obj = cachedObjectMap.get(definition);
            if (obj != null) {
                return (T) obj;
            }
            synchronized (definition) {
                obj = cachedObjectMap.get(definition);
                if (obj != null) {
                    return (T) obj;
                }
                String className = definition.clazz;
                ClassLoader classLoader = definition.classLoader;
                T srv = clazz.cast(ClassLoaderUtil.newInstance(classLoader, className));
                cachedObjectMap.putIfAbsent(definition, srv);
                return srv;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Service loader could not load name:" + name + "  class:" + clazz.getName() + "'s ServiceProvider from '" + LTS_DIRECTORY + "' or '" + LTS_INTERNAL_DIRECTORY + "' It may be empty or does not exist.");
        }
    }

    private static ServiceProvider getServiceProvider(Class<?> clazz) {
        ServiceProvider serviceProvider = serviceMap.get(clazz);
        if (serviceProvider == null) {
            getServiceProviders(clazz);
            serviceProvider = serviceMap.get(clazz);
        }
        return serviceProvider;
    }

    public static Set<String> getServiceProviders(final Class<?> clazz) {

        if (clazz == null)
            throw new IllegalArgumentException("type == null");
        if (!clazz.isInterface()) {
            throw new IllegalArgumentException(" type(" + clazz + ") is not interface!");
        }
        if (!clazz.isAnnotationPresent(SPI.class)) {
            throw new IllegalArgumentException("type(" + clazz +
                    ") is not extension, because WITHOUT @" + SPI.class.getSimpleName() + " Annotation!");
        }

        SPI spi = clazz.getAnnotation(SPI.class);
        String defaultName = spi.dftValue();
        String dynamicConfigKey = spi.key();


        final Set<URLDefinition> urlDefinitions = new HashSet<URLDefinition>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        urlDefinitions.addAll(collectExtensionUrls(LTS_DIRECTORY + clazz.getName(), classLoader));
        urlDefinitions.addAll(collectExtensionUrls(LTS_INTERNAL_DIRECTORY + clazz.getName(), classLoader));

        final ConcurrentMap<String, ServiceDefinition> serviceDefinitions = new ConcurrentHashMap<String, ServiceDefinition>();
        for (URLDefinition urlDefinition : urlDefinitions) {
            serviceDefinitions.putAll(parse(urlDefinition));
        }
        if (serviceDefinitions.isEmpty()) {
            throw new IllegalStateException("Service loader could not load " + clazz.getName() + "'s ServiceProvider from '" + LTS_DIRECTORY + "' or '" + LTS_INTERNAL_DIRECTORY + "' It may be empty or does not exist.");
        }
        ServiceProvider serviceProvider = new ServiceProvider(clazz, dynamicConfigKey, defaultName, serviceDefinitions);
        serviceMap.remove(clazz);   // 先移除
        serviceMap.put(clazz, serviceProvider);
        return serviceDefinitions.keySet();
    }

    private static Map<String, ServiceDefinition> parse(URLDefinition urlDefinition) {
        final Map<String, ServiceDefinition> nameClassMap = new HashMap<String, ServiceDefinition>();
        try {
            BufferedReader r = null;
            try {
                URL url = urlDefinition.uri.toURL();
                r = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
                while (true) {
                    String line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    int comment = line.indexOf('#');
                    if (comment >= 0) {
                        line = line.substring(0, comment);
                    }
                    line = line.trim();
                    if (line.length() == 0) {
                        continue;
                    }

                    int i = line.indexOf('=');
                    if (i > 0) {
                        String name = line.substring(0, i).trim();
                        String clazz = line.substring(i + 1).trim();
                        nameClassMap.put(name, new ServiceDefinition(name, clazz, urlDefinition.classLoader));
                    }
                }
            } finally {
                if (r != null) {
                    r.close();
                }
            }
        } catch (Exception e) {
            LOGGER.error("parse " + urlDefinition.uri + " error:" + e.getMessage(), e);
        }
        return nameClassMap;
    }

    private static Set<URLDefinition> collectExtensionUrls(String resourceName, ClassLoader classLoader) {
        try {
            final Enumeration<URL> configs;
            if (classLoader != null) {
                configs = classLoader.getResources(resourceName);
            } else {
                configs = ClassLoader.getSystemResources(resourceName);
            }

            Set<URLDefinition> urlDefinitions = new HashSet<URLDefinition>();
            while (configs.hasMoreElements()) {
                URL url = configs.nextElement();
                final URI uri = url.toURI();

                ClassLoader highestClassLoader = findHighestReachableClassLoader(url, classLoader, resourceName);
                urlDefinitions.add(new URLDefinition(uri, highestClassLoader));
            }
            return urlDefinitions;

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return Collections.emptySet();
    }

    private static ClassLoader findHighestReachableClassLoader(URL url, ClassLoader classLoader, String resourceName) {
        if (classLoader.getParent() == null) {
            return classLoader;
        }

        ClassLoader highestClassLoader = classLoader;

        ClassLoader current = classLoader;
        while (current.getParent() != null) {

            ClassLoader parent = current.getParent();

            try {
                Enumeration<URL> resources = parent.getResources(resourceName);
                if (resources != null) {
                    while (resources.hasMoreElements()) {
                        URL resourceURL = resources.nextElement();
                        if (url.toURI().equals(resourceURL.toURI())) {
                            highestClassLoader = parent;
                        }
                    }
                }
            } catch (IOException ignore) {
            } catch (URISyntaxException ignore) {
            }

            current = current.getParent();
        }
        return highestClassLoader;
    }

//    private static List<ClassLoader> selectClassLoaders(ClassLoader classLoader) {
//        List<ClassLoader> classLoaders = new ArrayList<ClassLoader>();
//
//        if (classLoader != null) {
//            classLoaders.add(classLoader);
//        }
//
//        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
//        if (currentClassLoader != classLoader) {
//            classLoaders.add(currentClassLoader);
//        }
//
//        ClassLoader coreClassLoader = ServiceLoader.class.getClassLoader();
//        if (coreClassLoader != classLoader && coreClassLoader != currentClassLoader) {
//            classLoaders.add(coreClassLoader);
//        }
//
//        return classLoaders;
//    }

    private static final class URLDefinition {

        private final URI uri;
        private final ClassLoader classLoader;

        private URLDefinition(URI url, ClassLoader classLoader) {
            this.uri = url;
            this.classLoader = classLoader;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            URLDefinition that = (URLDefinition) o;
            return uri != null ? uri.equals(that.uri) : that.uri == null;
        }

        @Override
        public int hashCode() {
            return uri != null ? uri.hashCode() : 0;
        }
    }

    private static final class ServiceDefinition {

        private final String name;
        private final String clazz;
        private final ClassLoader classLoader;

        private ServiceDefinition(String name, String clazz, ClassLoader classLoader) {
            Assert.notNull(name, "name");
            Assert.notNull(clazz, "clazz");
            Assert.notNull(classLoader, "classLoader");
            this.name = name;
            this.clazz = clazz;
            this.classLoader = classLoader;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ServiceDefinition that = (ServiceDefinition) o;

            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null) return false;
            return classLoader != null ? classLoader.equals(that.classLoader) : that.classLoader == null;

        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (clazz != null ? clazz.hashCode() : 0);
            result = 31 * result + (classLoader != null ? classLoader.hashCode() : 0);
            return result;
        }

    }

    private static final class ServiceProvider {
        private final Class<?> clazz;
        private final String defaultName;
        private final String dynamicConfigKey;
        private final ConcurrentMap<String, ServiceDefinition> nameMaps;

        public ServiceProvider(Class<?> clazz, String dynamicConfigKey, String defaultName, ConcurrentMap<String, ServiceDefinition> nameMaps) {
            this.clazz = clazz;
            this.dynamicConfigKey = dynamicConfigKey;
            this.defaultName = defaultName;
            this.nameMaps = nameMaps;
        }
    }
}
