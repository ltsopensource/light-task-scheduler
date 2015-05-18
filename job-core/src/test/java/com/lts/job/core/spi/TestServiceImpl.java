package com.lts.job.core.spi;

import com.lts.job.core.cluster.Config;

/**
 * Created by hugui on 5/18/15.
 */
public class TestServiceImpl implements TestService {
    @Override
    public void sayHello(Config config) {
        System.out.println("1");
    }

}
