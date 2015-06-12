package com.lts.zookeeper;


import com.lts.core.constant.Constants;
import com.lts.core.cluster.Config;
import com.lts.core.extension.Adaptive;
import com.lts.core.extension.SPI;

@SPI("zkclient")
public interface ZookeeperTransporter {

    @Adaptive({Constants.ZK_CLIENT_KEY})
    ZookeeperClient connect(Config config);

}
