package com.github.ltsopensource.core.failstore;

import com.github.ltsopensource.core.AppContext;

/**
 * @author Robert HG (254963746@qq.com) 4/1/16.
 */
public class FailStorePathBuilder {

    public static String getBizLoggerPath(AppContext appContext) {
        return getStorePath(appContext) + "/bizlog_failstore/";
    }

    public static String getJobFeedbackPath(AppContext appContext) {
        return getStorePath(appContext) + "/job_feedback_failstore/";
    }

    public static String getJobSubmitFailStorePath(AppContext appContext) {
        return getStorePath(appContext) + "/job_submit_failstore/";
    }

    public static String getDepJobSubmitFailStorePath(AppContext appContext) {
        return getStorePath(appContext) + "/dep_job_submit_failstore/";
    }

    private static String getStorePath(AppContext appContext) {
        return appContext.getConfig().getDataPath()
                + "/.lts" + "/" +
                appContext.getConfig().getNodeType() + "/" +
                appContext.getConfig().getNodeGroup();
    }
}
