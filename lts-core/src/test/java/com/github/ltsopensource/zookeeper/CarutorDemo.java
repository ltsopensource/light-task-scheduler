package com.github.ltsopensource.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Robert HG (254963746@qq.com) on 2/24/16.
 */
public class CarutorDemo {

    public static void main(String[] args) throws Exception {
        final CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(3000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        client.start();

        try {
            client.delete().forPath("/zk-lts-test/cnode");
        } catch (KeeperException.NoNodeException ignored) {
        }
        try {
            client.delete().forPath("/zk-lts-test/cnode2/3424");
        } catch (KeeperException.NoNodeException ignored) {
        }
        try {
            client.delete().forPath("/zk-lts-test/cnode2");
        } catch (KeeperException.NoNodeException ignored) {
        }

        client.create()
                .creatingParentsIfNeeded()
                .forPath("/zk-lts-test/cnode", "hello".getBytes());

        /**
         * 在注册监听器的时候，如果传入此参数，当事件触发时，逻辑由线程池处理
         */
        ExecutorService pool = Executors.newFixedThreadPool(2);

        /**
         * 监听数据节点的变化情况
         */
        final NodeCache nodeCache = new NodeCache(client, "/zk-lts-test/cnode", false);
        nodeCache.start(true);
        nodeCache.getListenable().addListener(
                new NodeCacheListener() {
                    @Override
                    public void nodeChanged() throws Exception {
                        if(nodeCache.getCurrentData().getData() == null){
                            System.out.println("delete data:" + nodeCache.getCurrentData().getPath());
                        }else{
                            System.out.println("Node data is changed, path:"+ nodeCache.getCurrentData().getPath() +" new data: " +
                                    new String(nodeCache.getCurrentData().getData()));
                        }
                    }
                },
                pool
        );

        /**
         * 监听子节点的变化情况
         */
        final PathChildrenCache childrenCache = new PathChildrenCache(client, "/zk-lts-test", true);
        childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        childrenCache.getListenable().addListener(
                new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework client2, PathChildrenCacheEvent event)
                            throws Exception {

                        switch (event.getType()) {
                            case CHILD_ADDED:
                            case CHILD_REMOVED:
                            case CHILD_UPDATED:
                                String childPath = event.getData().getPath();
                                String parentPath = childPath.substring(0, childPath.lastIndexOf("/"));
                                List<String> children = client.getChildren().forPath(parentPath);
                                System.out.println(event.getType() + "  " + children);
                            default:
                                break;
                        }
                    }
                },
                pool
        );

        client.setData().forPath("/zk-lts-test/cnode", "world".getBytes());

        client.create()
                .creatingParentsIfNeeded()
                .forPath("/zk-lts-test/cnode2", "hello".getBytes());

        client.create()
                .creatingParentsIfNeeded()
                .forPath("/zk-lts-test/cnode2/3424", "hello".getBytes());

        Thread.sleep(1000);

        client.setData().forPath("/zk-lts-test/cnode", null);

        Thread.sleep(10 * 1000);
        pool.shutdown();
        client.close();
    }
}
