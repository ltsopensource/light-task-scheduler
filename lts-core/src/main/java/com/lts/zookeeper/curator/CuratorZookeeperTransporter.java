package com.lts.zookeeper.curator;

import com.lts.core.cluster.Config;
import com.lts.zookeeper.ZookeeperClient;
import com.lts.zookeeper.ZookeeperTransporter;

public class CuratorZookeeperTransporter implements ZookeeperTransporter {

    public ZookeeperClient connect(Config config) {
        return new CuratorZookeeperClient(config);
    }

}