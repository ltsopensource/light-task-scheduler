package com.lts.job.core.registry;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.registry.redis.RedisRegistry;
import com.lts.job.core.registry.zookeeper.ZookeeperRegistry;
import com.lts.job.core.util.StringUtils;

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
            config.setRegistryAddress(
                    address.replace("zookeeper://", "")
            );
            return new ZookeeperRegistry(config);
        } else if (address.startsWith("redis://")) {
            config.setRegistryAddress(
                    address.replace("redis://", "")
            );
            return new RedisRegistry(config);
        }
        throw new IllegalArgumentException("illegal address protocol");
    }

}
