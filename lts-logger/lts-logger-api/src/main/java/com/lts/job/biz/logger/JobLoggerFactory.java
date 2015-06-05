package com.lts.job.biz.logger;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.extension.Adaptive;
import com.lts.job.core.extension.SPI;

/**
 * @author Robert HG (254963746@qq.com) on 5/19/15.
 */
@SPI("console")
public interface JobLoggerFactory {

    @Adaptive("job.logger")
    JobLogger getJobLogger(Config config);

}
