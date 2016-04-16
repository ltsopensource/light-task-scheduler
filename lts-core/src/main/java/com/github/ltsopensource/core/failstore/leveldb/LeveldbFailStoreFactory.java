package com.github.ltsopensource.core.failstore.leveldb;

import com.github.ltsopensource.core.failstore.AbstractFailStoreFactory;
import com.github.ltsopensource.core.failstore.FailStore;

import java.io.File;

/**
 * Robert HG (254963746@qq.com) on 5/21/15.
 */
public class LeveldbFailStoreFactory extends AbstractFailStoreFactory {

    @Override
    protected String getName() {
        return LeveldbFailStore.name;
    }

    @Override
    protected FailStore newInstance(File dbPath, boolean needLock) {
        return new LeveldbFailStore(dbPath, needLock);
    }
}
