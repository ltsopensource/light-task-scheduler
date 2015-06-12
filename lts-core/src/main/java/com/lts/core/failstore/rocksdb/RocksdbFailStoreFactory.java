package com.lts.core.failstore.rocksdb;

import com.lts.core.cluster.Config;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.failstore.FailStoreFactory;
import com.lts.core.failstore.FailStore;
import com.lts.core.failstore.leveldb.LeveldbFailStore;

/**
 * Robert HG (254963746@qq.com) on 5/27/15.
 */
public class RocksdbFailStoreFactory implements FailStoreFactory {

    @Override
    public FailStore getFailStore(Config config, String failStorePath) {
        if (StringUtils.isEmpty(failStorePath)) {
            failStorePath = config.getFailStorePath();
        }
        return new RocksdbFailStore(failStorePath);
    }
}
