package com.github.ltsopensource.core.registry;

import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.registry.redis.RedisRegistry;
import com.github.ltsopensource.core.registry.zookeeper.ZookeeperRegistry;

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
