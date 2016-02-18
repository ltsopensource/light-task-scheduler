package com.lts.zookeeper;


import com.lts.core.cluster.Config;
import com.lts.core.spi.SPI;
import com.lts.core.spi.SpiKey;

@SPI(key = SpiKey.ZK_CLIENT_KEY, dftValue = "zkclient")
public interface ZookeeperTransporter {

    ZkClient connect(Config config);

}
