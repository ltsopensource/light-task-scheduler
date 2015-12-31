package com.lts.core.loadbalance;

import com.lts.core.spi.SPI;
import com.lts.core.spi.SpiKey;

import java.util.List;

/**
 * Robert HG (254963746@qq.com) on 3/25/15.
 */
@SPI(key = SpiKey.LOADBALANCE, dftValue = "random")
public interface LoadBalance {

    public <S> S select(List<S> shards, String seed);

}
