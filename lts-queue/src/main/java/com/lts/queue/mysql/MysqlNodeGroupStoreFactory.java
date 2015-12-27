package com.lts.queue.mysql;

import com.lts.core.cluster.Config;
import com.lts.queue.NodeGroupStore;
import com.lts.queue.NodeGroupStoreFactory;

/**
 * @author Robert HG (254963746@qq.com) on 6/7/15.
 */
public class MysqlNodeGroupStoreFactory implements NodeGroupStoreFactory {

    @Override
    public NodeGroupStore getStore(Config config) {
        return new MysqlNodeGroupStore(config);
    }
}
