package com.lts.zookeeper.lts;

import com.lts.core.cluster.Config;
import com.lts.zookeeper.ZkClient;
import com.lts.zookeeper.ZookeeperTransporter;

public class LtsZookeeperTransporter implements ZookeeperTransporter {

    public ZkClient connect(Config config) {
        return new LtsZkClient(config);
    }

}
