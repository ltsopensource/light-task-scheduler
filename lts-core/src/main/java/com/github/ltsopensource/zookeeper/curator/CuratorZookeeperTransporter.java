package com.github.ltsopensource.zookeeper.curator;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.zookeeper.ZkClient;
import com.github.ltsopensource.zookeeper.ZookeeperTransporter;

public class CuratorZookeeperTransporter implements ZookeeperTransporter {

    public ZkClient connect(Config config) {
        return new CuratorZkClient(config);
    }

}