package com.lts.job.core.registry;

import com.lts.job.core.Application;
import com.lts.job.core.registry.redis.RedisRegistry;
import com.lts.job.core.registry.zookeeper.ZookeeperRegistry;
import com.lts.job.core.util.StringUtils;

/**
 * Created by hugui on 5/17/15.
 */
public class RegistryFactory {

    public static Registry getRegistry(Application application) {
        String address = application.getConfig().getRegistryAddress();
        if (StringUtils.isEmpty(address)) {
            throw new IllegalArgumentException("address is null！");
        }
        if (address.startsWith("zookeeper://")) {
            return new ZookeeperRegistry(application);
        } else if (address.startsWith("redis://")) {
            return new RedisRegistry(application);
        }
        throw new IllegalArgumentException("illegal address protocol");
    }

}
