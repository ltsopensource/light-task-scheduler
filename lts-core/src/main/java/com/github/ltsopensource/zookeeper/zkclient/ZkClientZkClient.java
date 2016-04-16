package com.github.ltsopensource.zookeeper.zkclient;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.registry.NodeRegistryUtils;
import com.github.ltsopensource.zookeeper.ChildListener;
import com.github.ltsopensource.zookeeper.DataListener;
import com.github.ltsopensource.zookeeper.StateListener;
import com.github.ltsopensource.zookeeper.support.AbstractZkClient;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
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
public class ZkClientZkClient extends AbstractZkClient<IZkChildListener, IZkDataListener> {

    public static final int connectionTimeout = 30000;

    private final ZkClient zkClient;

    private volatile KeeperState state = KeeperState.SyncConnected;

    public ZkClientZkClient(Config config) {
        String registryAddress = NodeRegistryUtils.getRealRegistryAddress(config.getRegistryAddress());
        zkClient = new ZkClient(registryAddress, connectionTimeout);

        zkClient.subscribeStateChanges(new IZkStateListener() {

            public void handleStateChanged(Watcher.Event.KeeperState state) throws Exception {
                ZkClientZkClient.this.state = state;
                if (state == KeeperState.Disconnected) {
                    stateChanged(StateListener.DISCONNECTED);
                } else if (state == KeeperState.SyncConnected) {
                    stateChanged(StateListener.CONNECTED);
                } else if (state == KeeperState.Expired) {
                    stateChanged(StateListener.DISCONNECTED);
                }
            }

            public void handleNewSession() throws Exception {
                stateChanged(StateListener.RECONNECTED);
            }
        });
    }

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

    protected IZkChildListener createTargetChildListener(String path, final ChildListener listener) {
        return new IZkChildListener() {
            public void handleChildChange(String parentPath, List<String> currentChildes)
                    throws Exception {
                listener.childChanged(parentPath, currentChildes);
            }
        };
    }

    protected List<String> addTargetChildListener(String path, IZkChildListener iZkChildListener) {
        return zkClient.subscribeChildChanges(path, iZkChildListener);
    }

    protected void removeTargetChildListener(String path, IZkChildListener iZkChildListener) {
        zkClient.unsubscribeChildChanges(path, iZkChildListener);
    }

    protected void addTargetDataListener(String path, IZkDataListener listener) {
        zkClient.subscribeDataChanges(path, listener);
    }

    protected IZkDataListener createTargetDataListener(String path, final DataListener listener) {
        return new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                listener.dataChange(dataPath, data);
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                listener.dataDeleted(dataPath);
            }
        };
    }

    protected void removeTargetDataListener(String path, IZkDataListener listener) {
        zkClient.unsubscribeDataChanges(path, listener);
    }

    public boolean delete(String path) {
        try {
            return zkClient.delete(path);
        } catch (ZkNoNodeException ignored) {
        }
        return false;
    }

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
