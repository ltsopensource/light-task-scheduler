package com.lts.core.loadbalance;

import com.lts.core.cluster.Config;
import com.lts.core.extension.Adaptive;
import com.lts.core.extension.SPI;

import java.util.List;

/**
 * Robert HG (254963746@qq.com) on 3/25/15.
 */
@SPI("random")
public interface LoadBalance {

    @Adaptive("loadbalance")
    public <S> S select(Config config, List<S> shards, String seed);

}
