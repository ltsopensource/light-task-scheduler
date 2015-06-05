package com.lts.job.core.loadbalance;

import com.lts.job.core.cluster.Config;

import java.util.List;

/**
 * Robert HG (254963746@qq.com) on 3/25/15.
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    @Override
    public <S> S select(Config config, List<S> shards, String seed) {
        if (shards == null || shards.size() == 0) {
            return null;
        }

        if (shards.size() == 1) {
            return shards.get(0);
        }

        return doSelect(shards, seed);
    }

    protected abstract <S> S doSelect(List<S> shards, String seed);

}
