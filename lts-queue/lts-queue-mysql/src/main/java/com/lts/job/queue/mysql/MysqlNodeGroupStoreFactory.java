package com.lts.job.queue.mysql;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.queue.NodeGroupStore;
import com.lts.job.queue.NodeGroupStoreFactory;
import com.lts.job.store.jdbc.JdbcRepository;

/**
 * Created by hugui on 6/7/15.
 */
public class MysqlNodeGroupStoreFactory implements NodeGroupStoreFactory {

    @Override
    public NodeGroupStore getStore(Config config) {
        return new MysqlNodeGroupStore(config);
    }
}
