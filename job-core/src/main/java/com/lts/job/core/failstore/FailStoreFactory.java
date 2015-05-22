package com.lts.job.core.failstore;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.extension.Adaptive;
import com.lts.job.core.extension.SPI;

/**
 * Created by hugui on 5/21/15.
 */
@SPI("leveldb")
public interface FailStoreFactory {

    @Adaptive("job.fail.store")
    public FailStore getFailStore(Config config);

}
