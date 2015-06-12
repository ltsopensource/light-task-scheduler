package com.lts.web.cluster;

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

}