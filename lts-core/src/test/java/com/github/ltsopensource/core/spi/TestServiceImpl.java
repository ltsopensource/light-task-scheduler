package com.github.ltsopensource.core.spi;

import com.github.ltsopensource.core.cluster.Config;

/**
 * @author Robert HG (254963746@qq.com) on 5/18/15.
 */
public class TestServiceImpl implements TestService {

    public TestServiceImpl() {
        System.out.println("1111111");
    }

    @Override
    public void sayHello(Config config) {
        System.out.println("1");
    }

}
