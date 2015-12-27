package com.lts.zookeeper;


import com.lts.core.cluster.Config;
import com.lts.core.constant.Constants;
import com.lts.core.spi.SPI;

@SPI(key = Constants.ZK_CLIENT_KEY, dftValue = "zkclient")
public interface ZookeeperTransporter {

    ZookeeperClient connect(Config config);

}
