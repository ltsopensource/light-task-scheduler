package com.lts.core.factory;

import com.lts.core.cluster.Config;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.constant.Constants;

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
        config.setInvokeTimeoutMillis(1000 * 60);
//        config.setListenPort(Constants.JOB_TRACKER_DEFAULT_LISTEN_PORT);
        config.setDataPath(Constants.USER_HOME);
        config.setClusterName(Constants.DEFAULT_CLUSTER_NAME);
        return config;
    }

}
