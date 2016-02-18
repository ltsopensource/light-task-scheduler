package com.lts.zookeeper.support;


import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.zookeeper.ChildListener;
import com.lts.zookeeper.StateListener;
import com.lts.zookeeper.ZkClient;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Robert HG (254963746@qq.com) on 7/8/14.
 */
public abstract class AbstractZkClient<TargetChildListener> implements ZkClient {

    private static final Logger logger = LoggerFactory.getLogger(AbstractZkClient.class);

    private final Set<StateListener> stateListeners = new CopyOnWriteArraySet<StateListener>();

    private final ConcurrentMap<String/*path*/, ConcurrentMap<ChildListener, TargetChildListener>> childListeners = new ConcurrentHashMap<String, ConcurrentMap<ChildListener, TargetChildListener>>();

    private volatile boolean closed = false;

    public String create(String path, boolean ephemeral, boolean sequential) {
        int i = path.lastIndexOf('/');
        if (i > 0) {
            create(path.substring(0, i), false, false);
        }
        if (ephemeral) {
            return createEphemeral(path, sequential);
        } else {
            return createPersistent(path, sequential);
        }
    }

    public String create(String path, Object data, boolean ephemeral, boolean sequential) {
        int i = path.lastIndexOf('/');
        if (i > 0) {
            create(path.substring(0, i), data, false, false);
        }
        if (ephemeral) {
            return createEphemeral(path, data, sequential);
        } else {
            return createPersistent(path, data, sequential);
        }
    }

    public Set<StateListener> getSessionListeners() {
        return stateListeners;
    }

    public void addStateListener(StateListener listener) {
        stateListeners.add(listener);
    }

    public void removeStateListener(StateListener listener) {
        stateListeners.remove(listener);
    }

    public List<String> addChildListener(String path, final ChildListener listener) {
        ConcurrentMap<ChildListener, TargetChildListener> listeners = childListeners.get(path);
        if (listeners == null) {
            childListeners.putIfAbsent(path, new ConcurrentHashMap<ChildListener, TargetChildListener>());
            listeners = childListeners.get(path);
        }
        TargetChildListener targetListener = listeners.get(listener);
        if (targetListener == null) {
            listeners.putIfAbsent(listener, createTargetChildListener(path, listener));
            targetListener = listeners.get(listener);
        }
        return addTargetChildListener(path, targetListener);
    }

    public void removeChildListener(String path, ChildListener listener) {
        ConcurrentMap<ChildListener, TargetChildListener> listeners = childListeners.get(path);
        if (listeners != null) {
            TargetChildListener targetListener = listeners.remove(listener);
            if (targetListener != null) {
                removeTargetChildListener(path, targetListener);
            }
        }
    }

    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            doClose();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    protected void stateChanged(int state) {
        for (StateListener stateListener : getSessionListeners()) {
            stateListener.stateChanged(state);
        }
    }


    protected abstract void doClose();

    protected abstract String createPersistent(String path, boolean sequential);

    protected abstract String createPersistent(String path, Object data, boolean sequential);

    protected abstract String createEphemeral(String path, boolean sequential);

    protected abstract String createEphemeral(String path, Object data, boolean sequential);

    protected abstract TargetChildListener createTargetChildListener(String path, ChildListener listener);

    protected abstract List<String> addTargetChildListener(String path, TargetChildListener listener);

    protected abstract void removeTargetChildListener(String path, TargetChildListener listener);

}
