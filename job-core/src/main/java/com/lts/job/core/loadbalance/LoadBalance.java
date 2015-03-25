package com.lts.job.core.loadbalance;

import java.util.List;

/**
 * Created by hugui on 3/25/15.
 */
public interface LoadBalance {

    public <S> S select(List<S> shards);

}
