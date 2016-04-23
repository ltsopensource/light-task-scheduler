package com.github.ltsopensource.core.registry;

import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.cluster.Node;
import com.github.ltsopensource.core.commons.concurrent.ConcurrentHashSet;
import com.github.ltsopensource.core.commons.utils.Callable;
import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.constant.ExtConfig;
import com.github.ltsopensource.core.factory.NamedThreadFactory;
import com.github.ltsopensource.core.support.NodeShutdownHook;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Robert HG (254963746@qq.com) on 5/17/15.
 */
public abstract class FailbackRegistry extends AbstractRegistry {

    // 定时任务执行器
    private final ScheduledExecutorService retryExecutor = Executors.newScheduledThreadPool(1,
            new NamedThreadFactory("LTSRegistryFailedRetryTimer", true));

    // 失败重试定时器，定时检查是否有请求失败，如有，无限次重试
    private ScheduledFuture<?> retryFuture;

    // 注册失败的定时重试
    private final Set<Node> failedRegistered = new ConcurrentHashSet<Node>();
    private final Set<Node> failedUnRegistered = new ConcurrentHashSet<Node>();
    private final ConcurrentMap<Node, Set<NotifyListener>> failedSubscribed = new ConcurrentHashMap<Node, Set<NotifyListener>>();
    private final ConcurrentMap<Node, Set<NotifyListener>> failedUnsubscribed = new ConcurrentHashMap<Node, Set<NotifyListener>>();
    private final ConcurrentMap<Node, Map<NotifyListener, NotifyPair<NotifyEvent, List<Node>>>> failedNotified = new ConcurrentHashMap<Node, Map<NotifyListener, NotifyPair<NotifyEvent, List<Node>>>>();

    public FailbackRegistry(AppContext appContext) {
        super(appContext);

        int retryPeriod = appContext.getConfig().getParameter(ExtConfig.REGISTRY_RETRY_PERIOD_KEY, Constants.DEFAULT_REGISTRY_RETRY_PERIOD);

        this.retryFuture = retryExecutor.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                // 检测并连接注册中心
                try {
                    retry();
                } catch (Throwable t) { // 防御性容错
                    LOGGER.error("Unexpected error occur at failed retry, cause: " + t.getMessage(), t);
                }
            }
        }, retryPeriod, retryPeriod, TimeUnit.MILLISECONDS);

        NodeShutdownHook.registerHook(appContext, this.getClass().getName(), new Callable() {
            @Override
            public void call() throws Exception {
                retryFuture.cancel(true);
                retryExecutor.shutdownNow();
                destroy();
            }
        });
    }

    @Override
    public void register(Node node) {
        try {
            super.register(node);
            failedRegistered.clear();
            doRegister(node);
        } catch (Exception e) {
            // 将失败的注册请求记录到失败列表，定时重试
            failedRegistered.add(node);
        }
    }

    @Override
    public void unregister(Node node) {
        try {
            super.unregister(node);
            failedUnRegistered.clear();
            doUnRegister(node);
        } catch (Exception e) {
            // 将失败的取消注册请求记录到失败列表，定时重试
            failedUnRegistered.add(node);
        }
    }

    @Override
    public void subscribe(Node node, NotifyListener listener) {
        try {
            super.subscribe(node, listener);

            removeFailedSubscribed(node, listener);

            doSubscribe(node, listener);

        } catch (Exception e) {
            addFailedSubscribed(node, listener);
        }
    }

    @Override
    public void unsubscribe(Node node, NotifyListener listener) {
        try {
            super.unsubscribe(node, listener);

            removeFailedSubscribed(node, listener);

            doUnsubscribe(node, listener);

        } catch (Exception e) {
            addFailedUnsubscribed(node, listener);
        }
    }

    protected void addFailedUnsubscribed(Node node, NotifyListener listener) {
        // 将失败的取消订阅请求记录到失败列表，定时重试
        Set<NotifyListener> listeners = failedUnsubscribed.get(node);
        if (listeners == null) {
            failedUnsubscribed.putIfAbsent(node, new ConcurrentHashSet<NotifyListener>());
            listeners = failedUnsubscribed.get(node);
        }
        listeners.add(listener);
    }

    @Override
    protected void notify(NotifyEvent event, List<Node> nodes, NotifyListener listener) {
        try {
            super.notify(event, nodes, listener);
        } catch (Exception e) {
            // 将失败的通知请求记录到失败列表，定时重试
            Map<NotifyListener, NotifyPair<NotifyEvent, List<Node>>> listeners = failedNotified.get(getNode());

            if (listeners == null) {
                failedNotified.putIfAbsent(getNode(), new ConcurrentHashMap<NotifyListener, NotifyPair<NotifyEvent, List<Node>>>());
                listeners = failedNotified.get(getNode());
            }
            listeners.put(listener, new NotifyPair<NotifyEvent, List<Node>>(event, nodes));
            LOGGER.error("Failed to notify, waiting for retry, cause: " + e.getMessage(), e);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            retryFuture.cancel(true);
        } catch (Throwable t) {
            LOGGER.warn(t.getMessage(), t);
        }
    }

    @Override
    protected void recover() throws Exception {
        // register
        Set<Node> recoverRegistered = new HashSet<Node>(getRegistered());
        if (!recoverRegistered.isEmpty()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Recover register node " + recoverRegistered);
            }
            for (Node node : recoverRegistered) {
                failedRegistered.add(node);
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
                    addFailedSubscribed(node, listener);
                }
            }
        }
    }

    private void removeFailedSubscribed(Node node, NotifyListener listener) {
        Set<NotifyListener> listeners = failedSubscribed.get(node);
        if (listeners != null) {
            listeners.remove(listener);
        }
        listeners = failedUnsubscribed.get(node);
        if (listeners != null) {
            listeners.remove(listener);
        }
        Map<NotifyListener, NotifyPair<NotifyEvent, List<Node>>> notified = failedNotified.get(node);
        if (notified != null) {
            notified.remove(listener);
        }
    }

    private void addFailedSubscribed(Node node, NotifyListener listener) {
        Set<NotifyListener> listeners = failedSubscribed.get(node);
        if (listeners == null) {
            failedSubscribed.putIfAbsent(node, new ConcurrentHashSet<NotifyListener>());
            listeners = failedSubscribed.get(node);
        }
        listeners.add(listener);
    }

    protected void retry() {
        if (!failedRegistered.isEmpty()) {
            Set<Node> failed = new HashSet<Node>(failedRegistered);
            if (failed.size() > 0) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Retry register {}", failed);
                }
                try {
                    for (Node node : failed) {
                        doRegister(node);
                        failedRegistered.remove(node);
                    }
                } catch (Throwable t) {     // 忽略所有异常，等待下次重试
                    LOGGER.warn("Failed to retry register " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
        if (!failedUnRegistered.isEmpty()) {
            Set<Node> failed = new HashSet<Node>(failedUnRegistered);
            if (failed.size() > 0) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Retry unregister {}", failed);
                }
                try {
                    for (Node node : failed) {
                        doUnRegister(node);
                        failedUnRegistered.remove(node);
                    }
                } catch (Throwable t) {     // 忽略所有异常，等待下次重试
                    LOGGER.warn("Failed to retry unregister " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
        if (!failedSubscribed.isEmpty()) {
            Map<Node, Set<NotifyListener>> failed = new HashMap<Node, Set<NotifyListener>>(failedSubscribed);
            for (Map.Entry<Node, Set<NotifyListener>> entry : new HashMap<Node, Set<NotifyListener>>(failed).entrySet()) {
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                    failed.remove(entry.getKey());
                }
            }
            if (failed.size() > 0) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Retry subscribe " + failed);
                }
                try {
                    for (Map.Entry<Node, Set<NotifyListener>> entry : failed.entrySet()) {
                        Node node = entry.getKey();
                        Set<NotifyListener> listeners = entry.getValue();
                        for (NotifyListener listener : listeners) {
                            try {
                                doSubscribe(node, listener);
                                listeners.remove(listener);
                                failedSubscribed.remove(entry.getKey());
                            } catch (Throwable t) { // 忽略所有异常，等待下次重试
                                LOGGER.warn("Failed to retry subscribe " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                            }
                        }
                    }
                } catch (Throwable t) { // 忽略所有异常，等待下次重试
                    LOGGER.warn("Failed to retry subscribe " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
        if (!failedUnsubscribed.isEmpty()) {
            Map<Node, Set<NotifyListener>> failed = new HashMap<Node, Set<NotifyListener>>(failedUnsubscribed);
            for (Map.Entry<Node, Set<NotifyListener>> entry : new HashMap<Node, Set<NotifyListener>>(failed).entrySet()) {
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                    failed.remove(entry.getKey());
                }
            }
            if (failed.size() > 0) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Retry unsubscribe " + failed);
                }
                try {
                    for (Map.Entry<Node, Set<NotifyListener>> entry : failed.entrySet()) {
                        Node node = entry.getKey();
                        Set<NotifyListener> listeners = entry.getValue();
                        for (NotifyListener listener : listeners) {
                            try {
                                doUnsubscribe(node, listener);
                                listeners.remove(listener);
                            } catch (Throwable t) { // 忽略所有异常，等待下次重试
                                LOGGER.warn("Failed to retry unsubscribe " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                            }
                        }
                    }
                } catch (Throwable t) { // 忽略所有异常，等待下次重试
                    LOGGER.warn("Failed to retry unsubscribe " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
        if (!failedNotified.isEmpty()) {
            Map<Node, Map<NotifyListener, NotifyPair<NotifyEvent, List<Node>>>> failed = new HashMap<Node, Map<NotifyListener, NotifyPair<NotifyEvent, List<Node>>>>(failedNotified);
            for (Map.Entry<Node, Map<NotifyListener, NotifyPair<NotifyEvent, List<Node>>>> entry : new HashMap<Node, Map<NotifyListener, NotifyPair<NotifyEvent, List<Node>>>>(failed).entrySet()) {
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                    failed.remove(entry.getKey());
                }
            }
            if (failed.size() > 0) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Retry notify " + failed);
                }
                try {
                    for (Map<NotifyListener, NotifyPair<NotifyEvent, List<Node>>> values : failed.values()) {
                        for (Map.Entry<NotifyListener, NotifyPair<NotifyEvent, List<Node>>> entry : values.entrySet()) {
                            try {
                                NotifyListener listener = entry.getKey();
                                NotifyPair<NotifyEvent, List<Node>> notifyPair = entry.getValue();
                                listener.notify(notifyPair.event, notifyPair.nodes);
                                values.remove(listener);
                            } catch (Throwable t) { // 忽略所有异常，等待下次重试
                                LOGGER.warn("Failed to retry notify " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                            }
                        }
                    }
                } catch (Throwable t) { // 忽略所有异常，等待下次重试
                    LOGGER.warn("Failed to retry notify " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
    }

    private class NotifyPair<T1, T2> {
        T1 event;
        T2 nodes;

        public NotifyPair(T1 event, T2 nodes) {
            this.event = event;
            this.nodes = nodes;
        }
    }

    protected abstract void doRegister(Node node);

    protected abstract void doUnRegister(Node node);

    protected abstract void doSubscribe(Node node, NotifyListener listener);

    protected abstract void doUnsubscribe(Node node, NotifyListener listener);
}
