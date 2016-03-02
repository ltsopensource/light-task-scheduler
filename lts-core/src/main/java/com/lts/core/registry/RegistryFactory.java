package com.lts.core.registry;

import com.lts.core.AppContext;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.registry.redis.RedisRegistry;
import com.lts.core.registry.zookeeper.ZookeeperRegistry;

/**
 * @author Robert HG (254963746@qq.com) on 5/17/15.
 */
public class RegistryFactory {

    public static Registry getRegistry(AppContext appContext) {

        String address = appContext.getConfig().getRegistryAddress();
        if (StringUtils.isEmpty(address)) {
            throw new IllegalArgumentException("address is null！");
        }
        if (address.startsWith("zookeeper://")) {
            return new ZookeeperRegistry(appContext);
        } else if (address.startsWith("redis://")) {
            return new RedisRegistry(appContext);
        } else if (address.startsWith("multicast://")) {
//            return new MulticastRegistry(config);
        }
        throw new IllegalArgumentException("illegal address protocol");
    }

}
