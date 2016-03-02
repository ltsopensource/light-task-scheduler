package com.lts.tasktracker.jobdispatcher;

import com.lts.core.commons.utils.ClassPathScanHandler;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.tasktracker.runner.JobRunner;

import java.util.*;

/**
 * 根据注解扫描作业，JobRunnerAnnotation
 * <p/>
 * Created by 28797575@qq.com hongliangpan on 2016/2/29.
 */
public class JobRunnerScanner {
    protected static final Logger LOGGER = LoggerFactory.getLogger(JobRunnerScanner.class);

    public static Map<String, JobRunner> scans(String scanPath) throws Exception {
        List<String> packages = new ArrayList<String>();
        packages.add(scanPath);
        return scans(packages);
    }

    public static Map<String, JobRunner> scans(String[] packages) throws Exception {
        return scans(Arrays.asList(packages));
    }

    public static Map<String, JobRunner> scans(List<String> packages) throws Exception {

        Map<String, JobRunner> map = new HashMap<String, JobRunner>();
        ClassPathScanHandler handler = new ClassPathScanHandler(true, false, null);

        for (String aPackage : packages) {
            Set<Class<?>> classes = handler.getPackageAllClasses(aPackage, true);
            if (CollectionUtils.isNotEmpty(classes)) {
                for (Class<?> clazz : classes) {
                    if (clazz.isAnnotationPresent(JobRunnerAnnotation.class)) {
                        JobRunnerAnnotation runnerTask = clazz.getAnnotation(JobRunnerAnnotation.class);
                        map.put(runnerTask.type(), (JobRunner) clazz.newInstance());
                    }
                }
            }
        }
        return map;
    }

}
