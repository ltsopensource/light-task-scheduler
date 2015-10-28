package com.lts.core.factory;

import com.lts.core.cluster.Config;
import com.lts.core.cluster.Node;
import com.lts.core.commons.utils.NetUtils;
import com.lts.core.support.SystemClock;

/**
 * @author Robert HG (254963746@qq.com) on 7/25/14.
 *         节点工厂类
 */
public class NodeFactory {

    public static <T extends Node> T create(Class<T> clazz, Config config) {
        try {
            T node = clazz.newInstance();
            node.setCreateTime(SystemClock.now());
            node.setIp(NetUtils.getLocalHost());
            node.setHostName(NetUtils.getLocalHostName());
            node.setGroup(config.getNodeGroup());
            node.setThreads(config.getWorkThreads());
            node.setPort(config.getListenPort());
            node.setIdentity(config.getIdentity());
            node.setClusterName(config.getClusterName());
            return node;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
