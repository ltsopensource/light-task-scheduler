package com.lts.job.core.failstore.rocksdb;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.failstore.FailStore;
import com.lts.job.core.failstore.FailStoreFactory;

/**
 * Robert HG (254963746@qq.com) on 5/27/15.
 */
public class RocksdbFailStoreFactory implements FailStoreFactory {

    @Override
    public FailStore getFailStore(Config config) {
        return new RocksdbFailStore(config);
    }
}
