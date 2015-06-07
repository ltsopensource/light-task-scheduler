package com.lts.job.queue;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.extension.Adaptive;
import com.lts.job.core.extension.SPI;

/**
 * @author Robert HG (254963746@qq.com) on 6/7/15.
 */
@SPI("mysql")
public interface NodeGroupStoreFactory {

    @Adaptive("job.queue")
    NodeGroupStore getStore(Config config);
}
