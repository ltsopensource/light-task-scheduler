package com.github.ltsopensource.core.failstore;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.commons.file.FileUtils;
import com.github.ltsopensource.core.commons.utils.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Robert HG (254963746@qq.com) on 11/10/15.
 */
public abstract class AbstractFailStoreFactory implements FailStoreFactory {
    @Override
    public final FailStore getFailStore(Config config, String storePath) {
        if (StringUtils.isEmpty(storePath)) {
            throw new IllegalStateException("storePath should not be empty");
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
