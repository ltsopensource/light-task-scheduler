package com.github.ltsopensource.biz.logger;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.spi.SPI;
import com.github.ltsopensource.core.constant.ExtConfig;

/**
 * @author Robert HG (254963746@qq.com) on 5/19/15.
 */
@SPI(key = ExtConfig.JOB_LOGGER, dftValue = "mysql")
public interface JobLoggerFactory {

    JobLogger getJobLogger(Config config);

}
