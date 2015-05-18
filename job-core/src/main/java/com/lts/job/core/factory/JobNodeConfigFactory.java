package com.lts.job.core.factory;

import com.lts.job.core.constant.Constants;
import com.lts.job.core.cluster.Config;
import com.lts.job.core.util.StringUtils;

/**
 * @author Robert HG (254963746@qq.com) on 3/30/15.
 */
public class JobNodeConfigFactory {

    public static Config getDefaultConfig() {
        Config config = new Config();
        config.setIdentity(StringUtils.generateUUID());
        config.setWorkThreads(Constants.AVAILABLE_PROCESSOR);
        config.setNodeGroup("lts");
        config.setRegistryAddress("zookeeper://127.0.0.1:2181");
        config.setInvokeTimeoutMillis(1000 * 6);
        config.setListenPort(0);
        config.setJobInfoSavePath(Constants.USER_HOME);
        config.setClusterName(Constants.DEFAULT_CLUSTER_NAME);
        return config;
    }

}
