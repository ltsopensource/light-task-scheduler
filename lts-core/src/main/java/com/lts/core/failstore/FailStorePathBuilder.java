package com.lts.core.failstore;

import com.lts.core.AppContext;

/**
 * Created by hugui.hg on 4/1/16.
 */
public class FailStorePathBuilder {

    public static String getBizLoggerPath(AppContext appContext) {
        return appContext.getConfig().getDataPath()
                + "/.lts" + "/" +
                appContext.getConfig().getNodeType() + "/" +
                appContext.getConfig().getNodeGroup() + "/bizlog_failstore/";
    }

    public static String getJobFeedbackPath(AppContext appContext) {
        return appContext.getConfig().getDataPath()
                + "/.lts" + "/" +
                appContext.getConfig().getNodeType() + "/" +
                appContext.getConfig().getNodeGroup() + "/job_feedback_failstore/";
    }

    public static String getJobSubmitFailStorePath(AppContext appContext) {
        return appContext.getConfig().getDataPath()
                + "/.lts" + "/" +
                appContext.getConfig().getNodeType() + "/" +
                appContext.getConfig().getNodeGroup() + "/job_submit_failstore/";
    }

    public static String getDepJobSubmitFailStorePath(AppContext appContext) {
        return appContext.getConfig().getDataPath()
                + "/.lts" + "/" +
                appContext.getConfig().getNodeType() + "/" +
                appContext.getConfig().getNodeGroup() + "/dep_job_submit_failstore/";
    }
}
