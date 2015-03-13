package com.lts.job.core.cluster;

import com.lts.job.core.domain.JobNodeConfig;
import com.lts.job.core.support.Application;
import com.lts.job.core.util.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert HG (254963746@qq.com) on 7/25/14.
 * 节点工厂类
 */
public class NodeFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeFactory.class);
    
    private Application application;
    private PathParser pathParser;
    
    public NodeFactory(Application application) {
        this.pathParser = new PathParser(application);
        this.application = application;
    }

    public <T extends Node> T create(Class<T> clazz, JobNodeConfig config) {
        try {
            T node = clazz.newInstance();
            node.setIp(NetUtils.getLocalHost());
            node.setGroup(config.getNodeGroup());
            node.setThreads(config.getWorkThreads());
            node.setPort(config.getListenPort());
            node.setIdentity(config.getIdentity());
            node.setPath(pathParser.getPath(node));
            return node;
        } catch (InstantiationException e ) {
            LOGGER.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public <T extends Node> T create(Class<T> clazz) {
        try {
            T node = clazz.newInstance();

            node.setIp(NetUtils.getLocalHost());
            node.setGroup(application.getConfig().getNodeGroup());
            node.setThreads(application.getConfig().getWorkThreads());
            node.setPort(application.getConfig().getListenPort());
            node.setIdentity(application.getConfig().getIdentity());
            node.setPath(pathParser.getPath(node));
            return node;
        } catch (InstantiationException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
