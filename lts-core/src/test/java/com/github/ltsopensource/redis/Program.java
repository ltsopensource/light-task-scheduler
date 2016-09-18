package com.github.ltsopensource.redis;

import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Program {

    public static final String CHANNEL_NAME = "commonChannel";

    private static Logger logger = LoggerFactory.getLogger(Program.class);

    public static void main(String[] args) throws Exception {

        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        final JedisPool jedisPool = new JedisPool(poolConfig, "localhost", 6379, 0);
        final Jedis subscriberJedis = jedisPool.getResource();
        final Subscriber subscriber = new Subscriber();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.info("Subscribing to \"commonChannel\". This thread will be blocked.");
                    subscriberJedis.subscribe(subscriber, CHANNEL_NAME);
                    logger.info("Subscription ended.");
                } catch (Exception e) {
                    logger.error("Subscribing failed.", e);
                }
            }
        }).start();

        final Jedis publisherJedis = jedisPool.getResource();

        new Publisher(publisherJedis, CHANNEL_NAME).start();

        subscriber.unsubscribe();
        subscriberJedis.close();
        publisherJedis.close();
        jedisPool.destroy();
    }
}