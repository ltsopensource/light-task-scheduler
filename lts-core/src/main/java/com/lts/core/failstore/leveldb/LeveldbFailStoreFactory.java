package com.lts.core.failstore.leveldb;

import com.lts.core.cluster.Config;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.failstore.FailStore;
import com.lts.core.failstore.FailStoreFactory;
import com.lts.core.failstore.berkeleydb.BerkeleydbFailStore;

/**
 * Robert HG (254963746@qq.com) on 5/21/15.
 */
public class LeveldbFailStoreFactory implements FailStoreFactory {
    @Override
    public FailStore getFailStore(Config config, String failStorePath) {
        if (StringUtils.isEmpty(failStorePath)) {
            failStorePath = config.getFailStorePath();
        }
        return new LeveldbFailStore(failStorePath);
    }
}
