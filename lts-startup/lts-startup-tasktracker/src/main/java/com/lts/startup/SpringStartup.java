package com.lts.startup;

import com.lts.tasktracker.TaskTracker;

import org.springframework.context.ApplicationContext;

/**
 * @author Robert HG (254963746@qq.com) on 9/11/15.
 */
public class SpringStartup {

    @SuppressWarnings("resource")
	public static TaskTracker start(String cfgPath) {

        System.setProperty("lts.tasktracker.cfg.path", cfgPath);

        ApplicationContext context = new LTSXmlApplicationContext(
                new String[]{"classpath*:spring/*.xml"}
        );
        return (TaskTracker) context.getBean("ltsTaskTracker");
    }

}
