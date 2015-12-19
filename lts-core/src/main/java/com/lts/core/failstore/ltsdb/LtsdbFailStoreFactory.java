package com.lts.core.failstore.ltsdb;

import com.lts.core.failstore.AbstractFailStoreFactory;
import com.lts.core.failstore.FailStore;

import java.io.File;

/**
 * @author Robert HG (254963746@qq.com) on 12/19/15.
 */
public class LtsdbFailStoreFactory extends AbstractFailStoreFactory {

    @Override
    protected String getName() {
        return LtsdbFailStore.name;
    }

    @Override
    protected FailStore newInstance(File dbPath, boolean needLock) {
        return new LtsdbFailStore(dbPath, needLock);
    }
}
