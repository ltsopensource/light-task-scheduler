package com.github.ltsopensource.core.failstore.mapdb;

import com.github.ltsopensource.core.failstore.AbstractFailStoreFactory;
import com.github.ltsopensource.core.failstore.FailStore;

import java.io.File;

/**
 * @author Robert HG (254963746@qq.com) on 11/10/15.
 */
public class MapdbFailStoreFactory extends AbstractFailStoreFactory {

    @Override
    protected String getName() {
        return MapdbFailStore.name;
    }

    @Override
    protected FailStore newInstance(File dbPath, boolean needLock) {
        return new MapdbFailStore(dbPath, needLock);
    }
}
