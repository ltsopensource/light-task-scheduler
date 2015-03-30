package com.lts.job.core.factory;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.domain.JobNodeConfig;
import com.lts.job.core.registry.PathParser;
import com.lts.job.core.util.NetUtils;

/**
 * @author Robert HG (254963746@qq.com) on 7/25/14.
 *         节点工厂类
 */
public class NodeFactory {

    public static <T extends Node> T create(PathParser pathParser, Class<T> clazz, JobNodeConfig config) {
        try {
            T node = clazz.newInstance();
            node.setIp(NetUtils.getLocalHost());
            node.setGroup(config.getNodeGroup());
            node.setThreads(config.getWorkThreads());
            node.setPort(config.getListenPort());
            node.setIdentity(config.getIdentity());
            node.setPath(pathParser.getPath(node));
            return node;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
