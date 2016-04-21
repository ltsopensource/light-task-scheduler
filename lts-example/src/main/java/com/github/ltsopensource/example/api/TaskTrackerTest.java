package com.github.ltsopensource.example.api;

import com.github.ltsopensource.tasktracker.TaskTracker;
import com.github.ltsopensource.tasktracker.TaskTrackerBuilder;


/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 */
public class TaskTrackerTest {

    public static void main(String[] args) {

        final TaskTracker taskTracker = new TaskTrackerBuilder()
                .setPropertiesConfigure("application.properties")
                .build();
        taskTracker.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                taskTracker.stop();
            }
        }));
    }
}