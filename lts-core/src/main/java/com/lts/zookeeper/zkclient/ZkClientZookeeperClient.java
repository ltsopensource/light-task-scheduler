package com.lts.zookeeper.zkclient;

import com.lts.core.cluster.Config;
import com.lts.core.registry.NodeRegistryUtils;
import com.lts.zookeeper.ChildListener;
import com.lts.zookeeper.StateListener;
import com.lts.zookeeper.support.AbstractZookeeperClient;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 7/8/14.
 */
public class ZkClientZookeeperClient extends AbstractZookeeperClient<IZkChildListener> {

    public static final int connectionTimeout = 30000;

    private final ZkClient zkClient;

    private volatile KeeperState state = KeeperState.SyncConnected;

    public ZkClientZookeeperClient(Config config) {
        String registryAddress = NodeRegistryUtils.getRealRegistryAddress(config.getRegistryAddress());
        zkClient = new ZkClient(registryAddress, connectionTimeout);

        zkClient.subscribeStateChanges(new IZkStateListener() {

            @Override
            public void handleStateChanged(Watcher.Event.KeeperState state) throws Exception {
                ZkClientZookeeperClient.this.state = state;
                if (state == KeeperState.Disconnected) {
                    stateChanged(StateListener.DISCONNECTED);
                } else if (state == KeeperState.SyncConnected) {
                    stateChanged(StateListener.CONNECTED);
                } else if (state == KeeperState.Expired) {
                    stateChanged(StateListener.DISCONNECTED);
                }
            }

            @Override
            public void handleNewSession() throws Exception {
                stateChanged(StateListener.RECONNECTED);
            }
        });
    }

    @Override
    protected String createPersistent(String path, boolean sequential) {
        try {
            if (sequential) {
                return zkClient.createPersistentSequential(path, true);
            } else {
                zkClient.createPersistent(path, true);
                return path;
            }
        } catch (ZkNodeExistsException ignored) {
        }
        return null;
    }

    @Override
    protected String createPersistent(String path, Object data, boolean sequential) {
        try {
            if (sequential) {
                return zkClient.createPersistentSequential(path, data);
            } else {
                zkClient.createPersistent(path, data);
                return path;
            }
        } catch (ZkNodeExistsException ignored) {
        }
        return null;
    }

    @Override
    protected String createEphemeral(String path, boolean sequential) {
        try {
            if (sequential) {
                return zkClient.createEphemeralSequential(path, true);
            } else {
                zkClient.createEphemeral(path);
                return path;
            }
        } catch (ZkNodeExistsException ignored) {
        }
        return null;
    }

    @Override
    protected String createEphemeral(String path, Object data, boolean sequential) {
        try {
            if (sequential) {
                return zkClient.createEphemeralSequential(path, data);
            } else {
                zkClient.createEphemeral(path, data);
                return path;
            }
        } catch (ZkNodeExistsException ignored) {
        }
        return null;
    }

    @Override
    protected IZkChildListener createTargetChildListener(String path, final ChildListener listener) {
        return new IZkChildListener() {
            public void handleChildChange(String parentPath, List<String> currentChildes)
                    throws Exception {
                listener.childChanged(parentPath, currentChildes);
            }
        };
    }

    @Override
    protected List<String> addTargetChildListener(String path, IZkChildListener iZkChildListener) {
        return zkClient.subscribeChildChanges(path, iZkChildListener);
    }

    @Override
    protected void removeTargetChildListener(String path, IZkChildListener iZkChildListener) {
        zkClient.unsubscribeChildChanges(path, iZkChildListener);
    }

    @Override
    public boolean delete(String path) {
        try {
            return zkClient.delete(path);
        } catch (ZkNoNodeException ignored) {
        }
        return false;
    }

    @Override
    public boolean exists(String path) {
        try {
            return zkClient.exists(path);
        } catch (ZkNoNodeException ignored) {
        }
        return false;
    }

    @Override
    public <T> T getData(String path) {
        return zkClient.readData(path);
    }

    @Override
    public void setData(String path, Object data) {
        zkClient.writeData(path, data);
    }

    @Override
    public List<String> getChildren(String path) {
        try {
            return zkClient.getChildren(path);
        } catch (ZkNoNodeException e) {
            return null;
        }
    }

    @Override
    public boolean isConnected() {
        return state == KeeperState.SyncConnected;
    }

    @Override
    protected void doClose() {
        zkClient.close();
    }
}
