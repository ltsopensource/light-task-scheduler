package com.lts.job.web.cluster;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.util.CollectionUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Robert HG (254963746@qq.com) on 6/5/15.
 */
public class RegistryServiceTest {

    @Test
    public void register() throws IOException {

        RegistryService registryService = new RegistryService();

        registryService.register("test_cluster");

        while (true) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<Node> nodeList = registryService.getAllNodes();
            if (CollectionUtils.isNotEmpty(nodeList)) {
                for (Node node : nodeList) {
                    System.out.println(node);
                }
            }
        }
    }

}