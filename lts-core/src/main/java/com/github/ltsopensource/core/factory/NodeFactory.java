package com.github.ltsopensource.core.factory;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.cluster.Node;
import com.github.ltsopensource.core.commons.utils.NetUtils;
import com.github.ltsopensource.core.exception.LtsRuntimeException;
import com.github.ltsopensource.core.support.SystemClock;

/**
 * @author Robert HG (254963746@qq.com) on 7/25/14.
 *         节点工厂类
 */
public class NodeFactory {

    public static <T extends Node> T create(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new LtsRuntimeException("Create Node error: clazz=" + clazz, e);
        }
    }

    public static void build(Node node, Config config) {
        node.setCreateTime(SystemClock.now());
        node.setIp(config.getIp());
        node.setHostName(NetUtils.getLocalHostName());
        node.setGroup(config.getNodeGroup());
        node.setThreads(config.getWorkThreads());
        node.setPort(config.getListenPort());
        node.setIdentity(config.getIdentity());
        node.setClusterName(config.getClusterName());
    }
}
