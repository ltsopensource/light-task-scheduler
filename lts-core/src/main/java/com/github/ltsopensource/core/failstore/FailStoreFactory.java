package com.github.ltsopensource.core.failstore;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.spi.SPI;
import com.github.ltsopensource.core.constant.ExtConfig;

/**
 * Robert HG (254963746@qq.com) on 5/21/15.
 */
@SPI(key = ExtConfig.FAIL_STORE, dftValue = "leveldb")
public interface FailStoreFactory {

    public FailStore getFailStore(Config config, String storePath);

}
