package com.lts.core.failstore;

import com.lts.core.cluster.Config;
import com.lts.core.extension.Adaptive;
import com.lts.core.extension.SPI;

/**
 * Robert HG (254963746@qq.com) on 5/21/15.
 */
@SPI("leveldb")
public interface FailStoreFactory {

    @Adaptive("job.fail.store")
    public FailStore getFailStore(Config config, String storePath);

}
