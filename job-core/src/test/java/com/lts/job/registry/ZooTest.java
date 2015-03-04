package com.lts.job.registry;

import com.lts.job.registry.zookeeper.ChildListener;
import com.lts.job.registry.zookeeper.ZookeeperClient;
import com.lts.job.registry.zookeeper.zkclient.ZkClientZookeeperClient;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 6/22/14.
 */
public class ZooTest {

    @Test
    public void test_connect() throws IOException {

        String address = "127.0.0.1:2181";
        ZookeeperClient client = new ZkClientZookeeperClient(address);
        System.out.println(client.isConnected());

        client.create("/lts/performance", true, false);

        System.out.println(client.getChildren("/lts"));

        client.addChildListener("/lts", new ChildListener() {
            @Override
            public void childChanged(String path, List<String> children) {
                System.out.println(path);
                System.out.println(children);
            }
        });

        client.delete("/lts/performance");

        System.in.read();
    }


    @Test
    public void test_disconnect() throws IOException {
        String address = "127.0.0.1:2181";
        ZkClientZookeeperClient client = new ZkClientZookeeperClient(address);
        System.out.println(client.isConnected());

        client.create("/lts/erp/test_", true, true);

        System.in.read();
    }

    @Test
    public void test_getData() throws IOException {
        String address = "127.0.0.1:2181";
        ZkClientZookeeperClient client = new ZkClientZookeeperClient(address);
        System.out.println(client.isConnected());

        client.create("/lts/erp/test_0", "dfasfa", true, false);

        System.out.println(client.getData("/lts/erp/test_0"));

        System.in.read();
    }

}
