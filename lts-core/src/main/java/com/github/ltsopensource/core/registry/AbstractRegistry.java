package com.github.ltsopensource.core.registry;

import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.cluster.Node;
import com.github.ltsopensource.core.commons.concurrent.ConcurrentHashSet;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Robert HG (254963746@qq.com) on 5/17/15.
 */
public abstract class AbstractRegistry implements Registry {

    protected final static Logger LOGGER = LoggerFactory.getLogger(Registry.class);

    private final Set<Node> registered = new ConcurrentHashSet<Node>();
    private final ConcurrentMap<Node, Set<NotifyListener>> subscribed = new ConcurrentHashMap<Node, Set<NotifyListener>>();

    protected AppContext appContext;
    private Node node;

    public AbstractRegistry(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void register(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("register node == null");
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Register: " + node);
        }
        registered.add(node);
    }

    @Override
    public void unregister(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("unregister node == null");
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Unregister: " + node);
        }
        registered.remove(node);
    }

    @Override
    public void subscribe(Node node, NotifyListener listener) {
        if (node == null) {
            throw new IllegalArgumentException("subscribe node == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("subscribe listener == null");
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Subscribe: " + node);
        }
        Set<NotifyListener> listeners = subscribed.get(node);
        if (listeners == null) {
            subscribed.putIfAbsent(node, new ConcurrentHashSet<NotifyListener>());
            listeners = subscribed.get(node);
        }
        listeners.add(listener);

    }

    @Override
    public void unsubscribe(Node node, NotifyListener listener) {
        if (node == null) {
            throw new IllegalArgumentException("unsubscribe node == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("unsubscribe listener == null");
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("unsubscribe: " + node);
        }
        Set<NotifyListener> listeners = subscribed.get(node);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    protected void notify(NotifyEvent event, List<Node> nodes, NotifyListener listener) {
        if (event == null) {
            throw new IllegalArgumentException("notify event == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("notify listener == null");
        }
        if (CollectionUtils.isEmpty(nodes)) {
            LOGGER.warn("Ignore empty notify nodes for subscribe node " + getNode());
            return;
        }

        listener.notify(event, nodes);
    }

    @Override
    public void destroy() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Destroy registry:" + getNode());
        }
        Set<Node> destroyRegistered = new HashSet<Node>(getRegistered());
        if (!destroyRegistered.isEmpty()) {
            for (Node node : new HashSet<Node>(getRegistered())) {
                try {
                    unregister(node);
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Destroy unregister node " + node);
                    }
                } catch (Throwable t) {
                    LOGGER.warn("Failed to unregister node " + node + " to registry " + getNode() + " on destroy, cause: " + t.getMessage(), t);
                }
            }
        }
        Map<Node, Set<NotifyListener>> destroySubscribed = new HashMap<Node, Set<NotifyListener>>(getSubscribed());
        if (!destroySubscribed.isEmpty()) {
            for (Map.Entry<Node, Set<NotifyListener>> entry : destroySubscribed.entrySet()) {
                Node node = entry.getKey();
                for (NotifyListener listener : entry.getValue()) {
                    try {
                        unsubscribe(node, listener);
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("Destroy unsubscribe node " + node);
                        }
                    } catch (Throwable t) {
                        LOGGER.warn("Failed to unsubscribe node " + node + " to registry " + getNode() + " on destroy, cause: " + t.getMessage(), t);
                    }
                }
            }
        }
    }

    protected Set<Node> getRegistered() {
        return registered;
    }

    protected ConcurrentMap<Node, Set<NotifyListener>> getSubscribed() {
        return subscribed;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    /**
     * 恢复
     *
     * @throws Exception
     */
    protected void recover() throws Exception {
        // register
        Set<Node> recoverRegistered = new HashSet<Node>(getRegistered());
        if (!recoverRegistered.isEmpty()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Recover register node " + recoverRegistered);
            }
            for (Node node : recoverRegistered) {
                register(node);
            }
        }
        // subscribe
        Map<Node, Set<NotifyListener>> recoverSubscribed = new HashMap<Node, Set<NotifyListener>>(getSubscribed());
        if (!recoverSubscribed.isEmpty()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Recover subscribe node " + recoverSubscribed.keySet());
            }
            for (Map.Entry<Node, Set<NotifyListener>> entry : recoverSubscribed.entrySet()) {
                Node node = entry.getKey();
                for (NotifyListener listener : entry.getValue()) {
                    subscribe(node, listener);
                }
            }
        }
    }

}
