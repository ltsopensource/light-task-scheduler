package com.lts.core.spi;

import com.lts.core.cluster.Config;
import com.lts.core.extension.Adaptive;
import com.lts.core.extension.SPI;

/**
 * @author Robert HG (254963746@qq.com) on 5/18/15.
 */
@SPI("test2")
public interface TestService {

    @Adaptive("test.type")
    public void sayHello(Config config);

}
