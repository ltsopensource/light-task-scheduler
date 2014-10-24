package com.lts.job.task.tracker;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 */
public class TaskTrackerStartup {

    public static void main(String[] args) {

        final TaskTracker taskTracker = new TaskTracker();
        taskTracker.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                taskTracker.stop();
            }
        }));
    }

}
