package com.lts.zookeeper.curator;

import com.lts.core.cluster.Config;
import com.lts.core.registry.NodeRegistryUtils;
import com.lts.zookeeper.ChildListener;
import com.lts.zookeeper.StateListener;
import com.lts.zookeeper.serializer.SerializableSerializer;
import com.lts.zookeeper.serializer.ZkSerializer;
import com.lts.zookeeper.support.AbstractZkClient;
import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.framework.api.CuratorWatcher;
import com.netflix.curator.framework.state.ConnectionState;
import com.netflix.curator.framework.state.ConnectionStateListener;
import com.netflix.curator.retry.RetryNTimes;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/16/15.
 */
public class CuratorZkClient extends AbstractZkClient<CuratorWatcher> {

    private final CuratorFramework client;
    private final ZkSerializer zkSerializer;

    public CuratorZkClient(Config config) {
        try {
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
                    } else if(state == ConnectionState.SUSPENDED){
                        CuratorZkClient.this.stateChanged(StateListener.DISCONNECTED);
                    }
                }
            });

            zkSerializer = new SerializableSerializer();

            client.start();

        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
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

    @Override
    protected CuratorWatcher createTargetChildListener(String path, ChildListener listener) {
        return new CuratorWatcherImpl(listener);
    }

    @Override
    protected List<String> addTargetChildListener(String path, CuratorWatcher listener) {
        try {
            return client.getChildren().usingWatcher(listener).forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    protected void removeTargetChildListener(String path, CuratorWatcher listener) {
        ((CuratorWatcherImpl) listener).unwatch();
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

    private class CuratorWatcherImpl implements CuratorWatcher {

        private volatile ChildListener listener;

        public CuratorWatcherImpl(ChildListener listener) {
            this.listener = listener;
        }

        public void unwatch() {
            this.listener = null;
        }

        public void process(WatchedEvent event) throws Exception {
            if(event.getPath() == null){
                return ;
            }
            if (listener != null) {
                listener.childChanged(event.getPath(), client.getChildren().usingWatcher(this).forPath(event.getPath()));
            }
        }
    }
}
