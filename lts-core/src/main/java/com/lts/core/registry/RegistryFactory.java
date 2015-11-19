package com.lts.core.registry;

import com.lts.core.Application;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.registry.redis.RedisRegistry;
import com.lts.core.registry.zookeeper.ZookeeperRegistry;

/**
 * @author Robert HG (254963746@qq.com) on 5/17/15.
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
        } else if (address.startsWith("multicast://")) {
//            return new MulticastRegistry(config);
        }
        throw new IllegalArgumentException("illegal address protocol");
    }

}
