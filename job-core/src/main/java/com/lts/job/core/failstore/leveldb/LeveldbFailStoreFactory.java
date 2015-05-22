package com.lts.job.core.failstore.leveldb;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.failstore.FailStore;
import com.lts.job.core.failstore.FailStoreFactory;

/**
 * Created by hugui on 5/21/15.
 */
public class LeveldbFailStoreFactory implements FailStoreFactory {
    @Override
    public FailStore getFailStore(Config config) {
        return new LeveldbFailStore(config);
    }
}
