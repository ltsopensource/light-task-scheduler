package com.lts.biz.logger;

import com.lts.core.cluster.Config;
import com.lts.core.extension.Adaptive;
import com.lts.core.extension.SPI;

/**
 * @author Robert HG (254963746@qq.com) on 5/19/15.
 */
@SPI("console")
public interface JobLoggerFactory {

    @Adaptive("job.logger")
    JobLogger getJobLogger(Config config);

}
