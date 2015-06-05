package com.lts.job.zookeeper.curator;

import com.lts.job.core.cluster.Config;
import com.lts.job.zookeeper.ZookeeperClient;
import com.lts.job.zookeeper.ZookeeperTransporter;

public class CuratorZookeeperTransporter implements ZookeeperTransporter {

    public ZookeeperClient connect(Config config) {
        return new CuratorZookeeperClient(config);
    }

}