package com.github.ltsopensource.zookeeper;


import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.spi.SPI;
import com.github.ltsopensource.core.spi.SpiExtensionKey;

@SPI(key = SpiExtensionKey.ZK_CLIENT_KEY, dftValue = "zkclient")
public interface ZookeeperTransporter {

    ZkClient connect(Config config);

}
