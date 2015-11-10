package com.lts.core.failstore;

import com.lts.core.cluster.Config;
import com.lts.core.commons.file.FileUtils;
import com.lts.core.commons.utils.StringUtils;

import java.io.File;

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
        FileUtils.createDirIfNotExist(dbPath);
        return newInstance(dbPath);
    }

    protected abstract String getName();

    protected abstract FailStore newInstance(File dbPath);
}
