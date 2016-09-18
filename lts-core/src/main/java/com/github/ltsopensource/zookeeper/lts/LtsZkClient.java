package com.github.ltsopensource.zookeeper.lts;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.registry.NodeRegistryUtils;
import com.github.ltsopensource.zookeeper.ChildListener;
import com.github.ltsopensource.zookeeper.DataListener;
import com.github.ltsopensource.zookeeper.StateListener;
import com.github.ltsopensource.zookeeper.serializer.SerializableSerializer;
import com.github.ltsopensource.zookeeper.serializer.ZkSerializer;
import com.github.ltsopensource.zookeeper.support.AbstractZkClient;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * LTS 自带实现的zkclient
 * @author Robert HG (254963746@qq.com) on 2/18/16.
 */
public class LtsZkClient extends AbstractZkClient<ChildListener, DataListener> implements Watcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(LtsZkClient.class);
    public static final int connectionTimeout = 30000;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition conditionNotConnect = lock.newCondition();
    private final Condition conditionConnected = lock.newCondition();
    private ZooKeeper zk;
    private volatile boolean isClosed = false;
    private String hosts;
    private volatile Event.KeeperState state = Event.KeeperState.SyncConnected;
    private ZkSerializer serializer = new SerializableSerializer();
    private final Map<String, Set<ChildListener>> childListeners = new ConcurrentHashMap<String, Set<ChildListener>>();

    public LtsZkClient(Config config) {

        this.hosts = NodeRegistryUtils.getRealRegistryAddress(config.getRegistryAddress());

        Thread guard = new Thread("zkClientGuard") {
            @Override
            public void run() {
                while (!isClosed) {
                    lock.lock();
                    try {
                        if (zk == null || !zk.getState().isAlive()) {
                            LOGGER.info("is not alive, try close before and new connect to zk");
                            tryReConnect();
                        }
                        conditionNotConnect.await(1000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        LOGGER.error("guard error, sleep 1000 to retry", e);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }
        };
        guard.setDaemon(true);
        guard.start();
    }

    @Override
    public void process(WatchedEvent event) {

        fireStateChanged(event);

        if (event.getType() == Watcher.Event.EventType.None) {
            if (Event.KeeperState.SyncConnected == event.getState()) {
                LOGGER.info("connected signal from zk: " + hosts
                        + ", sessionId=" + getSessionId()
                        + ", sessionTimeout="
                        + getSessionTimeout());
                notifyConnected();



            } else if (Event.KeeperState.Disconnected == event.getState()) {
                LOGGER.info("disconnected signal from zk: " + hosts
                        + ", sessionId=" + getSessionId()
                        + ", sessionTimeout=" + getSessionTimeout());
                notifyNotConnect();
            } else if (Event.KeeperState.Expired == event.getState()) {
                LOGGER
                        .info("expired signal from zk: " + hosts
                                + ", sessionId=" + getSessionId()
                                + ", sessionTimeout=" + getSessionTimeout());
                notifyNotConnect();
            }
        }

        childEventWatch(event);
    }

    private void fireStateChanged(WatchedEvent event) {
        // 记录当前状态
        this.state = event.getState();

        if (state == Event.KeeperState.Disconnected) {
            stateChanged(StateListener.DISCONNECTED);
        } else if (state == Event.KeeperState.SyncConnected) {
            stateChanged(StateListener.CONNECTED);
        } else if (state == Event.KeeperState.Expired) {
            stateChanged(StateListener.DISCONNECTED);
        }
    }

    private void childEventWatch(WatchedEvent event) {
        if (event.getPath() == null) {
            return;
        }

        try {
            // child Changed
            if (event.getType() == Event.EventType.NodeChildrenChanged || event.getType() == Event.EventType.NodeCreated || event.getType() == Event.EventType.NodeDeleted) {
                final String path = event.getPath();
                fireChangeEvent(path, childListeners.get(path));
            }
        } finally {
            if (event.getState() == Event.KeeperState.Expired) {
                for (Map.Entry<String, Set<ChildListener>> entry : childListeners.entrySet()) {
                    fireChangeEvent(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private void fireChangeEvent(String path, Set<ChildListener> listeners) {
        if (listeners != null && !listeners.isEmpty()) {
            for (ChildListener listener : listeners) {
                try {
                    exists(path);
                    List<String> children = getChildren(path);
                    listener.childChanged(path, children);
                } catch (ZkException e) {
                    if (e.isZkNoNodeException()) {
                        listener.childChanged(path, null);
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    private void notifyConnected() {
        lock.lock();
        try {
            conditionConnected.signal();
        } finally {
            lock.unlock();
        }
    }

    private synchronized void tryReConnect() throws IOException {
        doClose0();
        zk = new ZooKeeper(hosts, connectionTimeout, this);
    }

    private void notifyNotConnect() {
        lock.lock();
        try {
            conditionNotConnect.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected void doClose() {
        isClosed = true;
        doClose0();
    }

    private void doClose0() {
        if (this.zk != null) {
            try {
                this.zk.close();
            } catch (Exception e) {
                LOGGER.error("close error ", e);
            } finally {
                zk = null;
            }
        }
    }

    @Override
    protected String createPersistent(String path, boolean sequential) {
        return createPersistent(path, null, sequential);
    }

    @Override
    protected String createPersistent(String path, Object data, boolean sequential) {
        checkConnect();

        final byte[] bytes = data == null ? null : serialize(data);

        if (sequential) {
            try {
                return zk.create(path, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
            } catch (KeeperException e) {
                throw new ZkException("path[\" + path + \"], sequential[true] , code:" + e.code(), e);
            } catch (InterruptedException e) {
                throw new ZkInterruptedException("create persistent path[" + path + "], sequential[true]", e);
            }
        } else {
            try {
                zk.create(path, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                return path;
            } catch (KeeperException e) {
                if (isZkNodeExistsException(e)) {
                    // 已经存在 ignored
                } else if (isZkNoNodeException(e)) {
                    // 创建父亲节点
                    String parentDir = path.substring(0, path.lastIndexOf('/'));
                    createPersistent(parentDir, false);
                    createPersistent(path, false);
                } else {
                    throw new ZkException("path[\" + path + \"], sequential[false] , code:" + e.code(), e);
                }
            } catch (InterruptedException e) {
                throw new ZkInterruptedException("create persistent path[" + path + "], sequential[false]", e);
            }
        }
        return null;
    }

    private byte[] serialize(Object data) {
        return serializer.serialize(data);
    }

    @Override
    protected String createEphemeral(String path, boolean sequential) {
        return createEphemeral(path, null, sequential);
    }

    @Override
    protected String createEphemeral(String path, Object data, boolean sequential) {
        checkConnect();

        final byte[] bytes = data == null ? null : serialize(data);

        if (sequential) {
            try {
                return zk.create(path, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            } catch (KeeperException e) {
                throw new ZkException("path[\" + path + \"], sequential[true] , code:" + e.code(), e);
            } catch (InterruptedException e) {
                throw new ZkInterruptedException("create ephemeral path[" + path + "], sequential[true]", e);
            }
        } else {
            try {
                zk.create(path, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                return path;
            } catch (KeeperException e) {
                if (isZkNodeExistsException(e)) {
                    // 已经存在 ignored
                } else {
                    throw new ZkException("path[\" + path + \"], sequential[false] , code:" + e.code(), e);
                }
            } catch (InterruptedException e) {
                throw new ZkInterruptedException("create ephemeral path[" + path + "], sequential[false]", e);
            }
        }
        return null;
    }

    @Override
    protected ChildListener createTargetChildListener(String path, ChildListener listener) {
        return listener;
    }

    @Override
    protected List<String> addTargetChildListener(String path, ChildListener childListener) {
        checkConnect();

        synchronized (childListeners) {
            Set<ChildListener> listeners = childListeners.get(path);
            if (listeners == null) {
                listeners = new CopyOnWriteArraySet<ChildListener>();
                childListeners.put(path, listeners);
            }
            listeners.add(childListener);
        }
        try {
            zk.exists(path, true);
            try {
                return zk.getChildren(path, true);
            } catch (KeeperException e) {
                if (!isZkNoNodeException(e)) {
                    throw e;
                }
            }
        } catch (KeeperException e) {
            throw new ZkException(e);
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        }
        return null;
    }

    @Override
    protected void removeTargetChildListener(String path, ChildListener childListener) {
        synchronized (childListeners) {
            Set<ChildListener> listeners = childListeners.get(path);
            if (listeners != null) {
                listeners.remove(childListener);
            }
        }
    }

    @Override
    protected void addTargetDataListener(String path, DataListener listener) {

    }

    @Override
    protected DataListener createTargetDataListener(String path, DataListener listener) {
        return null;
    }

    @Override
    protected void removeTargetDataListener(String path, DataListener listener) {

    }

    @Override
    public boolean delete(String path) {
        try {
            zk.delete(path, -1);
            return true;
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        } catch (KeeperException e) {
            if (isZkNoNodeException(e)) {
                return false;
            } else {
                throw new ZkException(e);
            }
        }
    }

    private boolean isZkNoNodeException(KeeperException e) {
        return KeeperException.Code.NONODE == e.code();
    }

    private boolean isZkNodeExistsException(KeeperException e) {
        return KeeperException.Code.NODEEXISTS == e.code();
    }

    @Override
    public boolean exists(String path) {
        boolean watch = CollectionUtils.isNotEmpty(childListeners.get(path));
        try {
            return zk.exists(path, watch) != null;
        } catch (KeeperException e) {
            if (isZkNoNodeException(e)) {
                return false;
            } else {
                throw new ZkException(e);
            }
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        }
    }

    @Override
    public <T> T getData(String path) {
        // 暂时不watch data change 所以第二个参数为false
        try {
            byte[] data = zk.getData(path, false, null);
            return (T) serializer.deserialize(data);
        } catch (KeeperException e) {
            throw new ZkException(e);
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        }
    }

    @Override
    public void setData(String path, Object data) {
        byte[] bytes = serializer.serialize(data);
        try {
            zk.setData(path, bytes, -1);
        } catch (KeeperException e) {
            throw new ZkException(e);
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        }
    }

    @Override
    public List<String> getChildren(String path) {
        boolean watch = CollectionUtils.isNotEmpty(childListeners.get(path));
        try {
            return zk.getChildren(path, watch);
        } catch (KeeperException e) {
            if (isZkNoNodeException(e)) {
                return null;
            } else {
                throw new ZkException(e);
            }
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        }
    }

    @Override
    public boolean isConnected() {
        return this.state == Event.KeeperState.SyncConnected;
    }

    public String getSessionId() {
        ZooKeeper current = zk;
        if (current != null) {
            return String.format("0x%x", current.getSessionId());
        }
        return "";
    }

    public long getSessionTimeout() {
        ZooKeeper current = zk;
        if (current != null) {
            return current.getSessionTimeout();
        }
        return -1;
    }

    private void checkConnect() {
        if (zk == null || !zk.getState().isConnected()) {
            lock.lock();
            try {
                conditionConnected.await(10000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                LOGGER.error(e);
            } finally {
                lock.unlock();
            }
            if (zk == null || !zk.getState().isConnected()) {
                throw new ZkException("zk not connected, please wait");
            }
        }
    }

}
