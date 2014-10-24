package com.lts.job.tracker;

/**
 * @author Robert HG (254963746@qq.com) on 7/23/14.
 */
public class JobTrackerStartup {

    public static void main(String[] args) {

        final JobTracker jobTracker = new JobTracker();
        jobTracker.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                jobTracker.stop();
            }
        }));
    }

}
