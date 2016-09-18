package com.github.ltsopensource.zookeeper.lts;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.zookeeper.ZkClient;
import com.github.ltsopensource.zookeeper.ZookeeperTransporter;

public class LtsZookeeperTransporter implements ZookeeperTransporter {

    public ZkClient connect(Config config) {
        return new LtsZkClient(config);
    }

}
