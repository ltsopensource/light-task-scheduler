package com.lts.zookeeper.zkclient;

import com.lts.core.cluster.Config;
import com.lts.zookeeper.ZookeeperClient;
import com.lts.zookeeper.ZookeeperTransporter;

public class ZkClientZookeeperTransporter implements ZookeeperTransporter {

    public ZookeeperClient connect(Config config) {
        return new ZkClientZookeeperClient(config);
    }

}
