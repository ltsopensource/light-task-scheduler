package com.lts.job.core.spi;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.extension.Adaptive;
import com.lts.job.core.extension.SPI;

/**
 * @author Robert HG (254963746@qq.com) on 5/18/15.
 */
@SPI("test2")
public interface TestService {

    @Adaptive("test.type")
    public void sayHello(Config config);

}
