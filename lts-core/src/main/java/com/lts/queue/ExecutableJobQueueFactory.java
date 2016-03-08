package com.lts.queue;

import com.lts.core.cluster.Config;
import com.lts.core.spi.SPI;
import com.lts.core.spi.SpiExtensionKey;

/**
 * @author Robert HG (254963746@qq.com) on 5/30/15.
 */
@SPI(key = SpiExtensionKey.JOB_QUEUE, dftValue = "mysql")
public interface ExecutableJobQueueFactory {

    ExecutableJobQueue getQueue(Config config);

}