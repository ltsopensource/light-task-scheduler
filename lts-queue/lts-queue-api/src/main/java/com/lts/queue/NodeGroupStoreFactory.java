package com.lts.queue;

import com.lts.core.cluster.Config;
import com.lts.core.extension.Adaptive;
import com.lts.core.extension.SPI;

/**
 * @author Robert HG (254963746@qq.com) on 6/7/15.
 */
@SPI("mysql")
public interface NodeGroupStoreFactory {

    @Adaptive("job.queue")
    NodeGroupStore getStore(Config config);
}
