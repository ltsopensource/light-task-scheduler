package com.github.ltsopensource.startup.tasktracker;

import com.github.ltsopensource.tasktracker.TaskTracker;
import org.springframework.context.ApplicationContext;

/**
 * @author Robert HG (254963746@qq.com) on 9/11/15.
 */
public class SpringStartup {

    @SuppressWarnings("resource")
    public static TaskTracker start(TaskTrackerCfg cfg, String cfgPath) {

        System.setProperty("lts.tasktracker.cfg.path", cfgPath);

        String[] springXmlPaths = cfg.getSpringXmlPaths();

        String[] paths;

        if (springXmlPaths != null) {
            paths = new String[springXmlPaths.length + 1];
            paths[0] = "classpath:spring/lts-startup.xml";
            System.arraycopy(springXmlPaths, 0, paths, 1, springXmlPaths.length);
        } else {
            paths = new String[]{"classpath*:spring/*.xml"};
        }

        ApplicationContext context = new LTSXmlApplicationContext(paths);
        return (TaskTracker) context.getBean("ltsTaskTracker");
    }

}
