package com.lts.queue;

import com.lts.core.cluster.Config;
import com.lts.core.spi.SPI;
import com.lts.core.spi.SpiExtensionKey;

/**
 * @author Robert HG (254963746@qq.com) on 6/7/15.
 */
@SPI(key = SpiExtensionKey.JOB_QUEUE, dftValue = "mysql")
public interface NodeGroupStoreFactory {

    NodeGroupStore getStore(Config config);
}
