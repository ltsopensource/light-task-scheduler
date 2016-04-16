package com.github.ltsopensource.core.loadbalance;

import com.github.ltsopensource.core.commons.concurrent.ThreadLocalRandom;

import java.util.List;

/**
 * 随机负载均衡算法
 * Robert HG (254963746@qq.com) on 3/25/15.
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    @Override
    protected <S> S doSelect(List<S> shards, String seed) {
        return shards.get(ThreadLocalRandom.current().nextInt(shards.size()));
    }
}
