package com.lts.zookeeper.zkclient;

import com.lts.core.cluster.Config;
import com.lts.zookeeper.ZkClient;
import com.lts.zookeeper.ZookeeperTransporter;

public class ZkClientZookeeperTransporter implements ZookeeperTransporter {

    public ZkClient connect(Config config) {
        return new ZkClientZkClient(config);
    }

}
