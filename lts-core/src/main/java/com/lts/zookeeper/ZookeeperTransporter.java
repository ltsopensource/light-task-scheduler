package com.lts.zookeeper;


import com.lts.core.cluster.Config;
import com.lts.core.spi.SPI;
import com.lts.core.spi.SKey;

@SPI(key = SKey.ZK_CLIENT_KEY, dftValue = "zkclient")
public interface ZookeeperTransporter {

    ZookeeperClient connect(Config config);

}
