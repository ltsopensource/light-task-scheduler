package com.github.ltsopensource.core.loadbalance;

import java.util.List;

/**
 * Robert HG (254963746@qq.com) on 5/31/15.
 */
public class RoundbinLoadBalance extends AbstractLoadBalance{
    @Override
    protected <S> S doSelect(List<S> shards, String seed) {
        // TODO
        return null;
    }
}
