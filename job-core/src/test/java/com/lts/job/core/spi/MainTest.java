package com.lts.job.core.spi;

import com.lts.job.core.Application;
import com.lts.job.core.cluster.Config;
import com.lts.job.core.extension.ExtensionLoader;

/**
 * Created by hugui on 5/18/15.
 */
public class MainTest {

    public static void main(String[] args) {

//        TestService testService = ExtensionLoader.getExtensionLoader(TestService.class).getExtension("test2");
        TestService testService = ExtensionLoader.getExtensionLoader(TestService.class).getAdaptiveExtension();
        Config config = new Config();
        config.setParameter("test.type", "test2");
        testService.sayHello(config);
        Application application = new Application() {
        };
    }

}
