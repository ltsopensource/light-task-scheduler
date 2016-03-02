package com.lts.zookeeper.curator;

import com.lts.core.cluster.Config;
import com.lts.zookeeper.ZkClient;
import com.lts.zookeeper.ZookeeperTransporter;

public class CuratorZookeeperTransporter implements ZookeeperTransporter {

    public ZkClient connect(Config config) {
        return new CuratorZkClient(config);
    }

}