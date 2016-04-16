package com.github.ltsopensource.core.spi;


import com.github.ltsopensource.core.cluster.Config;

/**
 * @author Robert HG (254963746@qq.com) on 5/18/15.
 */
public class TestServiceImpl2 implements TestService {

    public TestServiceImpl2() {
        System.out.println("2222222");
    }

    @Override
    public void sayHello(Config config) {
        System.out.println("2");
    }
}
