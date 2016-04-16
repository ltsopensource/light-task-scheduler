package com.github.ltsopensource.zookeeper.zkclient;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.zookeeper.ZkClient;
import com.github.ltsopensource.zookeeper.ZookeeperTransporter;

public class ZkClientZookeeperTransporter implements ZookeeperTransporter {

    public ZkClient connect(Config config) {
        return new ZkClientZkClient(config);
    }

}
