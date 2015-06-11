package com.lts.job.core.registry;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.registry.redis.RedisRegistry;
import com.lts.job.core.registry.zookeeper.ZookeeperRegistry;
import com.lts.job.core.commons.utils.StringUtils;

/**
 * @author Robert HG (254963746@qq.com) on 5/17/15.
 */
public class RegistryFactory {

    public static Registry getRegistry(Config config) {

        String address = config.getRegistryAddress();
        if (StringUtils.isEmpty(address)) {
            throw new IllegalArgumentException("address is null！");
        }
        if (address.startsWith("zookeeper://")) {
            return new ZookeeperRegistry(config);
        } else if (address.startsWith("redis://")) {
            return new RedisRegistry(config);
        } else if (address.startsWith("multicast://")) {
//            return new MulticastRegistry(config);
        }
        throw new IllegalArgumentException("illegal address protocol");
    }

}
