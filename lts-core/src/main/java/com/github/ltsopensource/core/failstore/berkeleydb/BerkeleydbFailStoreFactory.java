package com.github.ltsopensource.core.failstore.berkeleydb;

import com.github.ltsopensource.core.failstore.AbstractFailStoreFactory;
import com.github.ltsopensource.core.failstore.FailStore;

import java.io.File;

/**
 * Robert HG (254963746@qq.com) on 5/26/15.
 */
public class BerkeleydbFailStoreFactory extends AbstractFailStoreFactory{

    @Override
    protected String getName() {
        return BerkeleydbFailStore.name;
    }

    @Override
    protected FailStore newInstance(File dbPath, boolean needLock) {
        return new BerkeleydbFailStore(dbPath, needLock);
    }
}
