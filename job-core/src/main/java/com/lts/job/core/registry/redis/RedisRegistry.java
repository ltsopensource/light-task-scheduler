package com.lts.job.core.registry.redis;

import com.lts.job.core.Application;
import com.lts.job.core.cluster.Node;
import com.lts.job.core.constant.Constants;
import com.lts.job.core.exception.NodeRegistryException;
import com.lts.job.core.factory.NamedThreadFactory;
import com.lts.job.core.registry.FailbackRegistry;
import com.lts.job.core.registry.NodeRegistryUtils;
import com.lts.job.core.registry.NotifyListener;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;
import java.util.concurrent.*;

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

    public RedisRegistry(Application application) {
        super(application);

        this.clusterName = application.getConfig().getClusterName();

        GenericObjectPool.Config config = new GenericObjectPool.Config();
        // TODO 可以设置n多参数

        String address = application.getConfig().getRegistryAddress();

        String cluster = application.getConfig().getParameter("cluster", "failover");
        if (!"failover".equals(cluster) && !"replicate".equals(cluster)) {
            throw new IllegalArgumentException("Unsupported redis cluster: " + cluster + ". The redis cluster only supported failover or replicate.");
        }
        replicate = "replicate".equals(cluster);

        int i = address.indexOf(':');
        String host = address.substring(0, i);
        int port = Integer.parseInt(address.substring(i + 1));
        this.jedisPools.put(address, new JedisPool(config, host, port,
                Constants.DEFAULT_TIMEOUT));

        this.expirePeriod = application.getConfig().getParameter(Constants.SESSION_TIMEOUT_KEY, Constants.DEFAULT_SESSION_TIMEOUT);

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
//            JedisPool jedisPool = entry.getValue();
//            try {
//                Jedis jedis = jedisPool.getResource();
//                try {
//                    for (URL url : new HashSet<URL>(getRegistered())) {
//                        if (url.getParameter(Constants.DYNAMIC_KEY, true)) {
//                            String key = toCategoryPath(url);
//                            if (jedis.hset(key, url.toFullString(), String.valueOf(System.currentTimeMillis() + expirePeriod)) == 1) {
//                                jedis.publish(key, Constants.REGISTER);
//                            }
//                        }
//                    }
//                    if (admin) {
//                        clean(jedis);
//                    }
//                    if (! replicate) {
//                        break;//  如果服务器端已同步数据，只需写入单台机器
//                    }
//                } finally {
//                    jedisPool.returnResource(jedis);
//                }
//            } catch (Throwable t) {
//                logger.warn("Failed to write provider heartbeat to redis registry. registry: " + entry.getKey() + ", cause: " + t.getMessage(), t);
//            }
        }
    }

    @Override
    public void register(Node node) {
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
    public void unregister(Node node) {
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
    protected void doRegister(Node node) {

    }

    @Override
    protected void doUnRegister(Node node) {

    }

    @Override
    protected void doSubscribe(Node node, NotifyListener listener) {

    }

    @Override
    protected void doUnsubscribe(Node node, NotifyListener listener) {

    }

}
