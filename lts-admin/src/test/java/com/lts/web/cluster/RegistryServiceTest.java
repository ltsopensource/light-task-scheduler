package com.lts.web.cluster;

import com.lts.core.support.SystemClock;
import org.junit.Test;

import java.io.IOException;

/**
 * Robert HG (254963746@qq.com) on 6/5/15.
 */
public class RegistryServiceTest {

    @Test
    public void register() throws IOException {

        RegistryService registryService = new RegistryService();

        registryService.register("test_cluster");

    }

    @Test
    public void testTime(){
        Long time = SystemClock.now();
        System.out.println(time);
        System.out.println(time / 1000);
    }

}