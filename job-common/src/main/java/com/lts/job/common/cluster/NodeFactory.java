package com.lts.job.common.cluster;

import com.lts.job.common.domain.JobNodeConfig;
import com.lts.job.common.support.Application;
import com.lts.job.common.util.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert HG (254963746@qq.com) on 7/25/14.
 * 节点工厂类
 */
public class NodeFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeFactory.class);

    private NodeFactory() {
    }

    public static <T extends Node> T create(Class<T> clazz, JobNodeConfig config) {
        try {
            T node = clazz.newInstance();
            node.setIp(NetUtils.getLocalHost());
            node.setGroup(config.getNodeGroup());
            node.setThreads(config.getWorkThreads());
            node.setPort(config.getListenPort());
            node.setIdentity(config.getIdentity());
            node.setPath(PathUtils.getPath(node));
            return node;
        } catch (InstantiationException e ) {
            LOGGER.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public static <T extends Node> T create(Class<T> clazz) {
        try {
            T node = clazz.newInstance();

            node.setIp(NetUtils.getLocalHost());
            node.setGroup(Application.Config.getNodeGroup());
            node.setThreads(Application.Config.getWorkThreads());
            node.setPort(Application.Config.getListenPort());
            node.setIdentity(Application.Config.getIdentity());
            node.setPath(PathUtils.getPath(node));
            return node;
        } catch (InstantiationException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
