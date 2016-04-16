package com.github.ltsopensource.core.registry.redis;

import redis.clients.jedis.Jedis;

/**
 * 锁会自动释放
 * @author Robert HG (254963746@qq.com) on 9/9/15.
 */
public class RedisLock {

    private String lockKey;
    private String lockValue;
    private int expiredSeconds;

    public RedisLock(String lockKey, String lockValue, int expiredSeconds) {
        this.lockValue = lockValue;
        this.lockKey = lockKey;
        this.expiredSeconds = expiredSeconds;
    }

    public boolean acquire(Jedis jedis) {
        String value = jedis.get(lockKey);
        if (value == null) {
            boolean success = jedis.setnx(lockKey, lockValue) == 1;
            if (success) {
                jedis.expire(lockKey, expiredSeconds);
                return true;
            }
        } else if (lockValue.equals(value)) {
            jedis.expire(lockKey, expiredSeconds);
            return true;
        }
        return false;
    }

}
