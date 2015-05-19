package com.lts.job.core.registry.redis;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.constant.Constants;
import com.lts.job.core.exception.NodeRegistryException;
import com.lts.job.core.factory.NamedThreadFactory;
import com.lts.job.core.registry.FailbackRegistry;
import com.lts.job.core.registry.NodeRegistryUtils;
import com.lts.job.core.registry.NotifyEvent;
import com.lts.job.core.registry.NotifyListener;
import com.lts.job.core.util.CollectionUtils;
import org.apache.commons.pool.impl.GenericObjectPool;
import com.lts.job.core.logger.Logger;
import com.lts.job.core.logger.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robert HG (254963746@qq.com) on 5/17/15.
 */
public class RedisRegistry extends FailbackRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisRegistry.class);

    private final Map<String, JedisPool> jedisPools = new ConcurrentHashMap<String, JedisPool>();

    private String clusterName;
    private final ScheduledExecutorService expireExecutor = Executors.newScheduledThreadPool(1,
            new NamedThreadFactory("LTSRedisRegistryExpireTimer", true));
    private final ScheduledFuture<?> expireFuture;
    private final int expirePeriod;
    private boolean replicate;
    private final int reconnectPeriod;
    private final ConcurrentMap<String, Notifier> notifiers = new ConcurrentHashMap<String, Notifier>();

    public RedisRegistry(Config config) {
        super(config);
        this.clusterName = config.getClusterName();

        GenericObjectPool.Config redisConfig = new GenericObjectPool.Config();
        // TODO 可以设置n多参数

        String address = config.getRegistryAddress();

        String cluster = config.getParameter("cluster", "failover");
        if (!"failover".equals(cluster) && !"replicate".equals(cluster)) {
            throw new IllegalArgumentException("Unsupported redis cluster: " + cluster + ". The redis cluster only supported failover or replicate.");
        }
        replicate = "replicate".equals(cluster);

        this.reconnectPeriod = config.getParameter(Constants.REGISTRY_RECONNECT_PERIOD_KEY, Constants.DEFAULT_REGISTRY_RECONNECT_PERIOD);

        int i = address.indexOf(':');
        String host = address.substring(0, i);
        int port = Integer.parseInt(address.substring(i + 1));
        this.jedisPools.put(address, new JedisPool(redisConfig, host, port,
                Constants.DEFAULT_TIMEOUT));

        this.expirePeriod = config.getParameter(Constants.SESSION_TIMEOUT_KEY, Constants.DEFAULT_SESSION_TIMEOUT);

        this.expireFuture = expireExecutor.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                try {
                    deferExpired(); // 延长过期时间
                } catch (Throwable t) { // 防御性容错
                    LOGGER.error("Unexpected exception occur at defer expire time, cause: " + t.getMessage(), t);
                }
            }
        }, expirePeriod / 2, expirePeriod / 2, TimeUnit.MILLISECONDS);
    }

    private void deferExpired() {
        for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
            JedisPool jedisPool = entry.getValue();
            try {
                Jedis jedis = jedisPool.getResource();
                try {
                    for (Node node : new HashSet<Node>(getRegistered())) {
                        String fullPath = NodeRegistryUtils.getFullPath(clusterName, node);
                        String key = NodeRegistryUtils.getNodeTypePath(clusterName, node.getNodeType());
                        if (jedis.hset(key, fullPath, String.valueOf(System.currentTimeMillis() + expirePeriod)) == 1) {
                            jedis.publish(key, Constants.REGISTER);
                        }
                    }
                    if (!replicate) {
                        break;//  如果服务器端已同步数据，只需写入单台机器
                    }
                } finally {
                    jedisPool.returnResource(jedis);
                }
            } catch (Throwable t) {
                LOGGER.warn("Failed to write provider heartbeat to redis registry. registry: " + entry.getKey() + ", cause: " + t.getMessage(), t);
            }
        }
    }


    @Override
    protected void doRegister(Node node) {
        String key = NodeRegistryUtils.getNodeTypePath(clusterName, node.getNodeType());
        String value = NodeRegistryUtils.getFullPath(clusterName, node);
        String expire = String.valueOf(System.currentTimeMillis() + expirePeriod);
        boolean success = false;
        NodeRegistryException exception = null;
        for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
            JedisPool jedisPool = entry.getValue();
            try {
                Jedis jedis = jedisPool.getResource();
                try {
                    jedis.hset(key, value, expire);
                    jedis.publish(key, Constants.REGISTER);
                    success = true;
                    if (!replicate) {
                        break; //  如果服务器端已同步数据，只需写入单台机器
                    }
                } finally {
                    jedisPool.returnResource(jedis);
                }
            } catch (Throwable t) {
                exception = new NodeRegistryException("Failed to register node to redis registry. registry: " + entry.getKey() + ", node: " + node + ", cause: " + t.getMessage(), t);
            }
        }
        if (exception != null) {
            if (success) {
                LOGGER.warn(exception.getMessage(), exception);
            } else {
                throw exception;
            }
        }
    }

    @Override
    protected void doUnRegister(Node node) {
        String key = NodeRegistryUtils.getNodeTypePath(clusterName, node.getNodeType());
        String value = NodeRegistryUtils.getFullPath(clusterName, node);
        boolean success = false;
        NodeRegistryException exception = null;
        for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
            JedisPool jedisPool = entry.getValue();
            try {
                Jedis jedis = jedisPool.getResource();
                try {
                    jedis.hdel(key, value);
                    jedis.publish(key, Constants.UNREGISTER);
                    success = true;
                    if (!replicate) {
                        break; //  如果服务器端已同步数据，只需写入单台机器
                    }
                } finally {
                    jedisPool.returnResource(jedis);
                }
            } catch (Throwable t) {
                exception = new NodeRegistryException("Failed to unregister node to redis registry. registry: " + entry.getKey() + ", node: " + node + ", cause: " + t.getMessage(), t);
            }
        }
        if (exception != null) {
            if (success) {
                LOGGER.warn(exception.getMessage(), exception);
            } else {
                throw exception;
            }
        }
    }

    @Override
    protected void doSubscribe(Node node, NotifyListener listener) {

        List<NodeType> listenNodeTypes = node.getListenNodeTypes();
        if (CollectionUtils.isEmpty(listenNodeTypes)) {
            return;
        }
        for (NodeType listenNodeType : listenNodeTypes) {
            String listenNodePath = NodeRegistryUtils.getNodeTypePath(clusterName, listenNodeType);

            Notifier notifier = notifiers.get(listenNodePath);
            if (notifier == null) {
                Notifier newNotifier = new Notifier(listenNodePath);
                notifiers.putIfAbsent(listenNodePath, newNotifier);
                notifier = notifiers.get(listenNodePath);
                if (notifier == newNotifier) {
                    notifier.start();
                }
            }

            boolean success = false;
            NodeRegistryException exception = null;
            for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
                JedisPool jedisPool = entry.getValue();
                try {
                    Jedis jedis = jedisPool.getResource();
                    try {
                        doNotify(jedis, jedis.keys(listenNodePath), Arrays.asList(listener), NotifyEvent.ADD);
                        success = true;
                        break; // 只需读一个服务器的数据

                    } finally {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Throwable t) {
                    exception = new NodeRegistryException("Failed to unregister node to redis registry. registry: " + entry.getKey() + ", node: " + node + ", cause: " + t.getMessage(), t);
                }
            }
            if (exception != null) {
                if (success) {
                    LOGGER.warn(exception.getMessage(), exception);
                } else {
                    throw exception;
                }
            }

        }
    }

    @Override
    protected void doUnsubscribe(Node node, NotifyListener listener) {
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            expireFuture.cancel(true);
        } catch (Throwable t) {
            LOGGER.warn(t.getMessage(), t);
        }
        try {
            for (Notifier notifier : notifiers.values()) {
                notifier.shutdown();
            }
        } catch (Throwable t) {
            LOGGER.warn(t.getMessage(), t);
        }
        for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
            JedisPool jedisPool = entry.getValue();
            try {
                jedisPool.destroy();
            } catch (Throwable t) {
                LOGGER.warn("Failed to destroy the redis registry client. registry: " + entry.getKey() + ", cause: " + t.getMessage(), t);
            }
        }
    }

    private void doNotify(Jedis jedis, Collection<String> keys, Collection<NotifyListener> listeners, NotifyEvent event) {
        if (keys == null || keys.size() == 0
                || listeners == null || listeners.size() == 0) {
            return;
        }
        List<Node> result = new ArrayList<Node>();
        for (String key : keys) {

            Map<String, String> values = jedis.hgetAll(key);
            if (values != null && values.size() > 0) {
                for (Map.Entry<String, String> entry : values.entrySet()) {
                    Node node = NodeRegistryUtils.parse(entry.getKey());
                    result.add(node);
                }
            }
        }
        if (result == null || result.size() == 0) {
            return;
        }
        for (NotifyListener listener : listeners) {
            notify(event, result, listener);
        }

    }

    private void doNotify(Jedis jedis, String key, NotifyEvent event) {
        for (Map.Entry<Node, Set<NotifyListener>> entry : new HashMap<Node, Set<NotifyListener>>(getSubscribed()).entrySet()) {
            doNotify(jedis, Arrays.asList(key), new HashSet<NotifyListener>(entry.getValue()), event);
        }
    }

    private class NotifySub extends JedisPubSub {

        private final JedisPool jedisPool;

        public NotifySub(JedisPool jedisPool) {
            this.jedisPool = jedisPool;
        }

        @Override
        public void onMessage(String key, String msg) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("redis event: " + key + " = " + msg);
            }
            if (msg.equals(Constants.REGISTER)
                    || msg.equals(Constants.UNREGISTER)) {
                try {
                    Jedis jedis = jedisPool.getResource();
                    try {
                        NotifyEvent event = msg.equals(Constants.REGISTER) ? NotifyEvent.ADD : NotifyEvent.REMOVE;
                        doNotify(jedis, key, event);
                    } finally {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Throwable t) { // TODO 通知失败没有恢复机制保障
                    LOGGER.error(t.getMessage(), t);
                }
            }
        }

        @Override
        public void onPMessage(String pattern, String key, String msg) {
            onMessage(key, msg);
        }

        @Override
        public void onSubscribe(String key, int num) {
        }

        @Override
        public void onPSubscribe(String pattern, int num) {
        }

        @Override
        public void onUnsubscribe(String key, int num) {
        }

        @Override
        public void onPUnsubscribe(String pattern, int num) {
        }

    }

    private class Notifier extends Thread {

        private final String listenNodePath;

        private volatile Jedis jedis;

        private volatile boolean running = true;

        private final AtomicInteger connectSkip = new AtomicInteger();

        private final AtomicInteger connectSkiped = new AtomicInteger();

        private final Random random = new Random();

        private volatile int connectRandom;

        private void resetSkip() {
            connectSkip.set(0);
            connectSkiped.set(0);
            connectRandom = 0;
        }

        private boolean isSkip() {
            int skip = connectSkip.get(); // 跳过次数增长
            if (skip >= 10) { // 如果跳过次数增长超过10，取随机数
                if (connectRandom == 0) {
                    connectRandom = random.nextInt(10);
                }
                skip = 10 + connectRandom;
            }
            if (connectSkiped.getAndIncrement() < skip) { // 检查跳过次数
                return true;
            }
            connectSkip.incrementAndGet();
            connectSkiped.set(0);
            connectRandom = 0;
            return false;
        }

        public Notifier(String listenNodePath) {
            super.setDaemon(true);
            super.setName("LTSRedisSubscribe");
            this.listenNodePath = listenNodePath;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    if (!isSkip()) {
                        try {
                            for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
                                JedisPool jedisPool = entry.getValue();
                                try {
                                    jedis = jedisPool.getResource();
                                    try {
                                        jedis.psubscribe(new NotifySub(jedisPool), listenNodePath); // 阻塞
                                        break;
                                    } finally {
                                        jedisPool.returnBrokenResource(jedis);
                                    }
                                } catch (Throwable t) { // 重试另一台
                                    LOGGER.warn("Failed to subscribe node from redis registry. registry: " + entry.getKey() + ", cause: " + t.getMessage(), t);
                                    // 如果在单台redis的情况下，需要休息一会，避免空转占用过多cpu资源
                                    sleep(reconnectPeriod);
                                }
                            }
                        } catch (Throwable t) {
                            LOGGER.error(t.getMessage(), t);
                            sleep(reconnectPeriod);
                        }
                    }
                } catch (Throwable t) {
                    LOGGER.error(t.getMessage(), t);
                }
            }
        }

        public void shutdown() {
            try {
                running = false;
                jedis.disconnect();
            } catch (Throwable t) {
                LOGGER.warn(t.getMessage(), t);
            }
        }

    }
}
