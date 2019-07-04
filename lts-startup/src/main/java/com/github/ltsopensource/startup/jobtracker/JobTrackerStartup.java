package com.github.ltsopensource.startup.jobtracker;

/**
 * @author Robert HG (254963746@qq.com) on 9/1/15.
 */
public class JobTrackerStartup {

    public static void main(String[] args) {
        try {

            String confPath = args[0];

            JobTrackerCfg cfg = JobTrackerCfgLoader.load(confPath);

            // FIXME jobTracker.start();

            // Runtime.getRuntime().addShutdownHook(new Thread(() -> jobTracker.stop()));

        } catch (CfgException e) {
            System.err.println("JobTracker Startup Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
