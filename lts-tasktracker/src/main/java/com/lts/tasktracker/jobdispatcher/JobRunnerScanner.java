package com.lts.tasktracker.jobdispatcher;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.tasktracker.runner.JobRunner;
import org.reflections.Reflections;
import org.reflections.scanners.*;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.net.URL;
import java.util.*;

/**
 * 根据注解扫描作业，JobRunnerAnnotation
 * <p/>
 * Created by 28797575@qq.com hongliangpan on 2016/2/29.
 */
public class JobRunnerScanner {
    protected static final Logger LOGGER = LoggerFactory.getLogger(JobRunnerScanner.class);

    public static void scans(String scanPaths, Map<String, JobRunner> map) throws Exception {
        List<String> packages = Lists.newArrayList();
        packages.add(scanPaths);
        scans(packages, map);
    }

    public static void scans(String[] packages, Map<String, JobRunner> map) throws InstantiationException, IllegalAccessException {
        scans(Arrays.asList(packages), map);
    }

    public static void scans(List<String> packages, Map<String, JobRunner> map) throws InstantiationException, IllegalAccessException {
        Reflections reflections = getReflection(packages);
        Set<Class<?>> annotations = reflections.getTypesAnnotatedWith(JobRunnerAnnotation.class);
        for (Class<?> clazz : annotations) {
            JobRunnerAnnotation runnerTask = clazz.getAnnotation(JobRunnerAnnotation.class);
            map.put(runnerTask.type(), (JobRunner) clazz.newInstance());
        }
    }

    /**
     * 通过扫描，获取反射对象
     */
    private static Reflections getReflection(List<String> packNameList) {

        FilterBuilder filterBuilder = new FilterBuilder();
        for (String packName : packNameList) {
            filterBuilder = filterBuilder.includePackage(packName);
        }
        filterBuilder.includePackage(JobRunnerScanner.class.getPackage().getName());
        Predicate<String> filter = filterBuilder;

        Collection<URL> urlTotals = new ArrayList<URL>();
        for (String packName : packNameList) {
            Set<URL> urls = ClasspathHelper.forPackage(packName);
            urlTotals.addAll(urls);
        }

        return new Reflections(new ConfigurationBuilder().filterInputsBy(filter)
                .setScanners(new TypeAnnotationsScanner().filterResultsBy(filter)
                ).setUrls(urlTotals));
    }

    public static void main(String[] args) throws Exception {
        Map<String, JobRunner> map = Maps.newHashMap();
        new JobRunnerScanner().scans("com.glodon.ysg", map);
    }
}
