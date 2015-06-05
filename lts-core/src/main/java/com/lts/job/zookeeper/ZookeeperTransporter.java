package com.lts.job.zookeeper;


import com.lts.job.core.cluster.Config;
import com.lts.job.core.constant.Constants;
import com.lts.job.core.extension.Adaptive;
import com.lts.job.core.extension.SPI;

@SPI("zkclient")
public interface ZookeeperTransporter {

    @Adaptive({Constants.ZK_CLIENT_KEY})
    ZookeeperClient connect(Config config);

}
