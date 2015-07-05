package com.lts.core.failstore.berkeleydb;

import com.lts.core.cluster.Config;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.failstore.FailStore;
import com.lts.core.failstore.FailStoreFactory;

/**
 * Robert HG (254963746@qq.com) on 5/26/15.
 */
public class BerkeleydbFailStoreFactory implements FailStoreFactory {
    @Override
    public FailStore getFailStore(Config config, String storePath) {
        if (StringUtils.isEmpty(storePath)) {
            storePath = config.getFailStorePath();
        }
        return new BerkeleydbFailStore(storePath, config.getIdentity());
    }
}
