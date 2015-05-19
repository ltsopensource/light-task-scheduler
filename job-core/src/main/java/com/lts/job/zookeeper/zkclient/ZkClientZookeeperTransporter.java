package com.lts.job.zookeeper.zkclient;

import com.lts.job.core.cluster.Config;
import com.lts.job.zookeeper.ZookeeperClient;
import com.lts.job.zookeeper.ZookeeperTransporter;

public class ZkClientZookeeperTransporter implements ZookeeperTransporter {

    public ZookeeperClient connect(Config config) {
        return new ZkClientZookeeperClient(config);
    }

}
