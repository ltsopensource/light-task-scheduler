package com.github.ltsopensource.zookeeper.curator;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.registry.NodeRegistryUtils;
import com.github.ltsopensource.zookeeper.ChildListener;
import com.github.ltsopensource.zookeeper.DataListener;
import com.github.ltsopensource.zookeeper.StateListener;
import com.github.ltsopensource.zookeeper.lts.ZkException;
import com.github.ltsopensource.zookeeper.serializer.SerializableSerializer;
import com.github.ltsopensource.zookeeper.serializer.ZkSerializer;
import com.github.ltsopensource.zookeeper.support.AbstractZkClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 5/16/15.
 */
public class CuratorZkClient extends AbstractZkClient<CuratorZkClient.PathChildrenListener, CuratorZkClient.NodeListener> {

    private final CuratorFramework client;
    private final ZkSerializer zkSerializer;

    public CuratorZkClient(Config config) {
        String registryAddress = NodeRegistryUtils.getRealRegistryAddress(config.getRegistryAddress());
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(registryAddress)
                .retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 1000))
                .connectionTimeoutMs(5000);

        client = builder.build();

        client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            public void stateChanged(CuratorFramework client, ConnectionState state) {
                if (state == ConnectionState.LOST) {
                    CuratorZkClient.this.stateChanged(StateListener.DISCONNECTED);
                } else if (state == ConnectionState.CONNECTED) {
                    CuratorZkClient.this.stateChanged(StateListener.CONNECTED);
                } else if (state == ConnectionState.RECONNECTED) {
                    CuratorZkClient.this.stateChanged(StateListener.RECONNECTED);
                } else if (state == ConnectionState.SUSPENDED) {
                    CuratorZkClient.this.stateChanged(StateListener.DISCONNECTED);
                }
            }
        });

        zkSerializer = new SerializableSerializer();

        client.start();
    }

    @Override
    protected String createPersistent(String path, boolean sequential) {
        try {
            if (sequential) {
                return client.create().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(path);
            } else {
                return client.create().withMode(CreateMode.PERSISTENT).forPath(path);
            }
        } catch (KeeperException.NodeExistsException e) {
            return path;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    protected String createPersistent(String path, Object data, boolean sequential) {
        try {
            if (sequential) {
                byte[] zkDataBytes;
                if (data instanceof Serializable) {
                    zkDataBytes = zkSerializer.serialize(data);
                } else {
                    zkDataBytes = (byte[]) data;
                }
                return client.create().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(path, zkDataBytes);
            } else {
                return client.create().withMode(CreateMode.PERSISTENT).forPath(path);
            }
        } catch (KeeperException.NodeExistsException e) {
            return path;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    protected String createEphemeral(String path, boolean sequential) {
        try {
            if (sequential) {
                return client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path);
            } else {
                return client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
            }
        } catch (KeeperException.NodeExistsException e) {
            return path;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    protected String createEphemeral(String path, Object data, boolean sequential) {
        try {
            if (sequential) {
                byte[] zkDataBytes;
                if (data instanceof Serializable) {
                    zkDataBytes = zkSerializer.serialize(data);
                } else {
                    zkDataBytes = (byte[]) data;
                }
                return client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, zkDataBytes);
            } else {
                return client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
            }
        } catch (KeeperException.NodeExistsException e) {
            return path;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    protected PathChildrenListener createTargetChildListener(final String path, final ChildListener listener) {
        return new PathChildrenListener(path, listener);
    }

    protected List<String> addTargetChildListener(String path, PathChildrenListener listener) {
        try {
            listener.startListener();
            return client.getChildren().forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    protected void removeTargetChildListener(String path, PathChildrenListener listener) {
        listener.stopListener();
    }

    @Override
    protected void addTargetDataListener(String path, NodeListener listener) {
        listener.startListener();
    }

    @Override
    protected NodeListener createTargetDataListener(String path, final DataListener listener) {
        return new NodeListener(path, listener);
    }

    @Override
    protected void removeTargetDataListener(String path, NodeListener listener) {
        listener.stopListener();
    }

    @Override
    public boolean delete(String path) {
        try {
            client.delete().forPath(path);
            return true;
        } catch (KeeperException.NoNodeException e) {
            return true;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String path) {
        try {
            return client.checkExists().forPath(path) != null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getData(String path) {
        try {
            return (T) zkSerializer.deserialize(client.getData().forPath(path));
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void setData(String path, Object data) {
        byte[] zkDataBytes;
        if (data instanceof Serializable) {
            zkDataBytes = zkSerializer.serialize(data);
        } else {
            zkDataBytes = (byte[]) data;
        }
        try {
            client.setData().forPath(path, zkDataBytes);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> getChildren(String path) {
        try {
            return client.getChildren().forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isConnected() {
        return client.getZookeeperClient().isConnected();
    }

    @Override
    protected void doClose() {
        client.close();
    }

    public class PathChildrenListener {
        private PathChildrenCache childrenCache;
        private PathChildrenCacheListener childrenCacheListener;
        private AtomicBoolean start = new AtomicBoolean(false);

        public PathChildrenListener(String path, final ChildListener listener) {
            childrenCache = new PathChildrenCache(client, path, true);
            childrenCacheListener = new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework c, PathChildrenCacheEvent event)
                        throws Exception {

                    switch (event.getType()) {
                        case CHILD_ADDED:
                        case CHILD_REMOVED:
                        case CHILD_UPDATED:
                            String childPath = event.getData().getPath();
                            String parentPath = childPath.substring(0, childPath.lastIndexOf("/"));
                            List<String> children = client.getChildren().forPath(parentPath);
                            listener.childChanged(parentPath, children);
                        default:
                            break;
                    }
                }
            };
        }

        public void startListener() {
            try {
                if (start.compareAndSet(false, true)) {
                    childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
                    childrenCache.getListenable().addListener(childrenCacheListener);
                }
            } catch (Exception e) {
                throw new ZkException(e);
            }
        }

        public void stopListener() {
            try {
                if (start.compareAndSet(true, false)) {
                    childrenCache.getListenable().removeListener(childrenCacheListener);
                    childrenCache.clear();
                    childrenCache.close();
                }
            } catch (IOException e) {
                throw new ZkException(e);
            }
        }
    }

    public class NodeListener {
        private NodeCache nodeCache;
        private NodeCacheListener nodeCacheListener;
        private AtomicBoolean start = new AtomicBoolean(false);

        public NodeListener(String path, final DataListener listener) {
            nodeCache = new NodeCache(client, path, false);
            nodeCacheListener = new NodeCacheListener() {
                @Override
                public void nodeChanged() throws Exception {
                    String path = nodeCache.getCurrentData().getPath();

                    Object data = nodeCache.getCurrentData().getData();

                    if (data == null) {
                        listener.dataDeleted(path);
                    } else {
                        listener.dataChange(path, data);
                    }
                }
            };
        }

        public void startListener() {
            try {
                if (start.compareAndSet(false, true)) {
                    nodeCache.start(true);
                    nodeCache.getListenable().addListener(nodeCacheListener);
                }
            } catch (Exception e) {
                throw new ZkException(e);
            }
        }

        public void stopListener() {
            try {
                if (start.compareAndSet(true, false)) {
                    nodeCache.getListenable().removeListener(nodeCacheListener);
                    nodeCache.close();
                }
            } catch (IOException e) {
                throw new ZkException(e);
            }
        }
    }
}
