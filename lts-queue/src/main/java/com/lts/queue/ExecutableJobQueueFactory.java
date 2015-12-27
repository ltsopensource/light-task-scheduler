package com.lts.queue;

import com.lts.core.cluster.Config;
import com.lts.core.spi.SPI;

/**
 * @author Robert HG (254963746@qq.com) on 5/30/15.
 */
@SPI(key = "job.queue", dftValue = "mysql")
public interface ExecutableJobQueueFactory {

    ExecutableJobQueue getQueue(Config config);

}