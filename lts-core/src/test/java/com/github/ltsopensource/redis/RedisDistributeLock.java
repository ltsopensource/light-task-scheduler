package com.github.ltsopensource.redis;

import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author Robert HG (254963746@qq.com) on 9/9/15.
 */
public class RedisDistributeLock {

    private static Logger LOGGER = LoggerFactory.getLogger(RedisDistributeLock.class);

    private static JedisPool pool;
    private JedisLock        jedisLock;
    private String           lockKey;
    private Jedis jedis;
    private int              timeoutMsecs;
    private int              expireMsecs;

    public RedisDistributeLock(String lockKey) {
        this(lockKey, 3000, 300000);
    }

    public RedisDistributeLock(String lockKey, int timeoutMsecs, int expireMsecs) {
        this.lockKey = lockKey;
        this.jedis = pool.getResource();
        this.timeoutMsecs = timeoutMsecs;
        this.expireMsecs = expireMsecs;
        this.jedisLock = new JedisLock(jedis, lockKey.intern(), timeoutMsecs, expireMsecs);
    }

    public void wrap(Runnable runnable) {
        long begin = System.currentTimeMillis();
        try {
            // timeout超时，等待入锁的时间，设置为3秒；expiration过期，锁存在的时间设置为5分钟
            LOGGER.info("begin logck,lockKey={},timeoutMsecs={},expireMsecs={}", lockKey, timeoutMsecs, expireMsecs);
            if (jedisLock.acquire()) { // 启用锁
                runnable.run();
            } else {
                LOGGER.info("The time wait for lock more than [{}] ms ", timeoutMsecs);
            }
        } catch (Throwable t) {
            // 分布式锁异常
            LOGGER.warn(t.getMessage(), t);
        } finally {
            this.lockRelease(jedisLock, jedis);
        }
        LOGGER.info("[{}]cost={}", lockKey, System.currentTimeMillis() - begin);
    }

    /**
     * 释放锁,后期欲将离线计算的释放锁封装
     */
    private void lockRelease(JedisLock lock,
                             Jedis jedis) {
        if (lock != null) {
            try {
                lock.release();// 则解锁
            } catch (Exception e) {
            }
        }
        if (jedis != null) {
            jedis.close();
        }
        LOGGER.info("release logck,lockKey={},timeoutMsecs={},expireMsecs={}", lockKey, timeoutMsecs, expireMsecs);
    }

    public static JedisPool getPool() {
        return pool;
    }

    public static synchronized void setPool(JedisPool pool) {
        RedisDistributeLock.pool = pool;
    }

}
