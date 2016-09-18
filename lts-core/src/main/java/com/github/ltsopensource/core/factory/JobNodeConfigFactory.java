package com.github.ltsopensource.core.factory;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.core.commons.utils.DateUtils;
import com.github.ltsopensource.core.constant.Constants;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robert HG (254963746@qq.com) on 3/30/15.
 */
public class JobNodeConfigFactory {

    private static final AtomicInteger SEQ = new AtomicInteger(0);

    public static Config getDefaultConfig() {
        Config config = new Config();
        config.setWorkThreads(64);
        config.setNodeGroup("lts");
        config.setRegistryAddress("zookeeper://127.0.0.1:2181");
        config.setInvokeTimeoutMillis(1000 * 60);
        config.setDataPath(Constants.USER_HOME);
        config.setClusterName(Constants.DEFAULT_CLUSTER_NAME);
        return config;
    }

    public static void buildIdentity(Config config) {
        String sb = getNodeTypeShort(config.getNodeType()) +
                "_" +
                config.getIp() +
                "_" +
                getPid() +
                "_" +
                DateUtils.format(new Date(), "HH-mm-ss.SSS")
                + "_" + SEQ.incrementAndGet();
        config.setIdentity(sb);
    }

    private static String getNodeTypeShort(NodeType nodeType) {
        switch (nodeType) {
            case JOB_CLIENT:
                return "JC";
            case JOB_TRACKER:
                return "JT";
            case TASK_TRACKER:
                return "TT";
            case MONITOR:
                return "MO";
            case BACKEND:
                return "BA";
        }
        throw new IllegalArgumentException();
    }

    private static Integer getPid() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName();
        int index = name.indexOf("@");
        if (index != -1) {
            return Integer.parseInt(name.substring(0, index));
        }
        return 0;
    }

}
