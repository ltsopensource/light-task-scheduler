package com.lts.job.core.loadbalance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机负载均衡算法
 * Created by hugui on 3/25/15.
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    @Override
    protected <S> S doSelect(List<S> shards) {
        return shards.get(ThreadLocalRandom.current().nextInt(shards.size()));
    }
}
