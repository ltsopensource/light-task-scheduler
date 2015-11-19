package com.lts.queue;

import com.lts.core.Application;
import com.lts.core.cluster.Config;
import com.lts.core.extension.Adaptive;
import com.lts.core.extension.SPI;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/15.
 */
@SPI("mysql")
public interface PreLoaderFactory {

    @Adaptive("job.queue")
    public PreLoader getPreLoader(Config config, Application application);

}
