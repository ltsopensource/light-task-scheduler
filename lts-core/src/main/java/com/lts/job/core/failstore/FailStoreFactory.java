package com.lts.job.core.failstore;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.extension.Adaptive;
import com.lts.job.core.extension.SPI;

/**
 * Robert HG (254963746@qq.com) on 5/21/15.
 */
@SPI("leveldb")
public interface FailStoreFactory {

    @Adaptive("job.fail.store")
    public FailStore getFailStore(Config config);

}
