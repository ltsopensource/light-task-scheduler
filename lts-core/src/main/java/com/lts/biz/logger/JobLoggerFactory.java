package com.lts.biz.logger;

import com.lts.core.cluster.Config;
import com.lts.core.spi.SPI;
import com.lts.core.spi.SpiExtensionKey;

/**
 * @author Robert HG (254963746@qq.com) on 5/19/15.
 */
@SPI(key = SpiExtensionKey.JOB_LOGGER, dftValue = "console")
public interface JobLoggerFactory {

    JobLogger getJobLogger(Config config);

}
