package com.lts.tasktracker.jobdispatcher;

import com.google.common.io.Resources;
import com.lts.core.commons.utils.ClassHelper;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;


/**
 * tasktracker 嵌入client
 *
 * Created by 28797575@qq.com hongliangpan on 2016/2/29.
 */
public class EmbedJobClient {
    public static final String TASKTRACKER_CFG = "tasktracker.cfg";
    protected static final Logger LOGGER = LoggerFactory.getLogger(JobRunnerDispatcher.class);
    protected Properties conf;

    public Properties getConfig() {
        if (conf != null) {
            return conf;
        }
        URL url = Resources.getResource(TASKTRACKER_CFG);
        try {
            conf = new Properties();
            conf.load(new FileInputStream(new File(url.toURI())));
        } catch (Exception e) {
            LOGGER.error("can not find tasktracker.cfg", e);
        }
        return conf;
    }

    public String getConfig(String key) {
        Object value = getConfig().get(key);
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    protected boolean isEmbedJobClient() {
        return "true".equalsIgnoreCase(getConfig("isEmbedJobClient"));
    }

    public void submitJob() {
        if (!isEmbedJobClient()) {
            return;
        }
        try {
            String jobSubmitterClass = getConfig("jobSubmitterClass");
            JobSubmitter jobSubmitter = (JobSubmitter) ClassHelper.forName(jobSubmitterClass).newInstance();
            jobSubmitter.submitJob(conf);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

}