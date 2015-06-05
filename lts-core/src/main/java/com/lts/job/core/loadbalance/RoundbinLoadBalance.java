package com.lts.job.core.loadbalance;

import java.util.List;

/**
 * Created by hugui on 5/31/15.
 */
public class RoundbinLoadBalance extends AbstractLoadBalance{



    @Override
    protected <S> S doSelect(List<S> shards, String seed) {
        return null;
    }
}
