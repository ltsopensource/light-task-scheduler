package com.lts.core.failstore;

import com.lts.core.cluster.Config;
import com.lts.core.commons.file.FileUtils;
import com.lts.core.commons.utils.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Robert HG (254963746@qq.com) on 11/10/15.
 */
public abstract class AbstractFailStoreFactory implements FailStoreFactory {
    @Override
    public final FailStore getFailStore(Config config, String storePath) {
        if (StringUtils.isEmpty(storePath)) {
            storePath = config.getFailStorePath();
        }
        File dbPath = new File(storePath.concat(getName()).concat("/").concat(config.getIdentity()));
        try {
            FileUtils.createDirIfNotExist(dbPath);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return newInstance(dbPath, true);
    }

    protected abstract String getName();

    protected abstract FailStore newInstance(File dbPath, boolean needLock);
}
